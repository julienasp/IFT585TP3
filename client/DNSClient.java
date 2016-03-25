package client;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author JUASP-G73-Android
 */
public class DNSClient {
    private static final byte[] defaultAddr = new byte[]{8, 8, 8, 8};
    private static int uniqueID;
    private InetAddress dnsAddr;
    private static List respondQuestions;
    private static List<InetAddress> respondAnswers;
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(DNSClient.class);  

    public DNSClient() {
        this.respondQuestions = Collections.EMPTY_LIST;
        this.respondAnswers = Collections.EMPTY_LIST;
        try {             
            InetAddress dnsAddr = InetAddress.getByAddress(defaultAddr);
        } catch (UnknownHostException ex) {
            Logger.getLogger(DNSClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public InetAddress getDnsAddr() {
        return dnsAddr;
    }

    public void setDnsAddr(InetAddress dnsAddr) {
        this.dnsAddr = dnsAddr;
    }
    
    private static byte[] buildQuery(String domainName){
        logger.info("DNSClient: buildQuery(): executed!");
        ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream ();
        DataOutputStream dataOut = new DataOutputStream (byteArrayOut);
        try {
            dataOut.writeShort(uniqueID);
            dataOut.writeShort( (1 << 7) |(1 << 8) ); // Recursion Desired and Recursion Available
            dataOut.writeShort(1); // nb of questions
            dataOut.writeShort(0); // nb of answers
            dataOut.writeShort(0); // nb of authorities
            dataOut.writeShort(0); // nb of additional
            StringTokenizer labels = new StringTokenizer (domainName, ".");
            while (labels.hasMoreTokens ()) {
              String label = labels.nextToken ();
              dataOut.writeByte(label.length ());
              dataOut.writeBytes(label);
            }
            dataOut.writeByte(0);
            dataOut.writeShort(1); // Request Type A
            dataOut.writeShort(1); // Class Internet
          } catch (IOException ex) {
              Logger.getLogger(DNSClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return byteArrayOut.toByteArray ();
    } 
    private static void handleResponse (byte[] data, int length, int offset) throws IOException {
        logger.info("DNSClient: handleResponse(): executed!");
        ByteArrayInputStream bais = new ByteArrayInputStream (data, 0, length);
        DataInputStream dis = new DataInputStream(bais);
        int id = dis.readUnsignedShort();
        if (id != uniqueID) throw new IOException ("ID does not match request");
        int flags = dis.readUnsignedShort();        
        int nbQuestions = dis.readUnsignedShort(); 
        int nbAnswers = dis.readUnsignedShort(); 
        int nbAuthorities = dis.readUnsignedShort(); 
        int nbAdditional = dis.readUnsignedShort(); 
        logger.info("DNSClient: handleResponse(): id: " + id);
        logger.info("DNSClient: handleResponse(): flags: " + flags);
        logger.info("DNSClient: handleResponse(): nbQuestions: " + nbQuestions);
        logger.info("DNSClient: handleResponse(): nbAnswers: " + nbAnswers);
        logger.info("DNSClient: handleResponse(): nbAuthorities: " + nbAuthorities);
        logger.info("DNSClient: handleResponse(): nbAdditional: " + nbAdditional);
        // parse questions
        if (nbQuestions > 0){
            for (int i = 0; i < nbQuestions; i++){
                byte[] len = new byte[dis.readUnsignedByte()];
                dis.readFully(len);
                int short1 = dis.readUnsignedShort();
                int short2 = dis.readUnsignedShort();
                logger.info("DNSClient: handleResponse(): Question string: " + new String(len));
                logger.info("DNSClient: handleResponse(): Question short1: " + short1);
                logger.info("DNSClient: handleResponse(): Question short2: " + short2);
            }
        }
        
        respondAnswers = Collections.synchronizedList(new ArrayList(nbAnswers));
        
        for (int i = 0; i < nbAnswers; i++){
        
            byte[] lenString = new byte[dis.readUnsignedByte()];
            dis.readFully(lenString);
            String domain = new String(lenString);
            int type = dis.readUnsignedShort();
            int dnsClass = dis.readUnsignedShort();
            int ttl = (dis.readUnsignedShort() << 16) + dis.readUnsignedShort();;
            int len = dis.readUnsignedShort();
            
            logger.info("DNSClient: handleResponse(): Answer string: " + new String(lenString));
            logger.info("DNSClient: handleResponse(): Answer Type: " + type);
            logger.info("DNSClient: handleResponse(): Answer dnsClass: " + dnsClass);
            logger.info("DNSClient: handleResponse(): Answer ttl: " + ttl);
            logger.info("DNSClient: handleResponse(): Answer len: " + len);
            
            int end = offset + len;
            logger.info("DNSClient: handleResponse(): la valeur de type est: " + type);
            if(type == 1){ // TYPE A
                byte[] adrTypeA = new byte[len];
                System.arraycopy(data, offset, adrTypeA, 0, len); 
                respondAnswers.add(InetAddress.getByAddress(adrTypeA));
            }            
        }
        
        
        
    }
 
    private static void sendQuery (String domainName, DatagramSocket socketDNS, InetAddress nameServer) throws IOException {
        logger.info("DNSClient: sendQuery(): executed!");
        byte[] data = buildQuery(domainName);
        DatagramPacket packet = new DatagramPacket(data, data.length, nameServer, 53); // 53 is the default port
        socketDNS.send(packet);
    }
    
    private static void getResponse (DatagramSocket socketDNS) throws IOException {
        logger.info("DNSClient: getResponse(): executed!"); 
        byte[] buffer = new byte[512]; // MAX SIZE UDP PACKET
        DatagramPacket packet = new DatagramPacket (buffer, buffer.length);
        socketDNS.receive(packet);
        logger.info("DNSClient: getResponse():" + new String(packet.getData())); 
        handleResponse(packet.getData(), packet.getLength(), packet.getOffset());
    }
    
    public static InetAddress resolveDomain(String domain){        
        try {
            logger.info("DNSClient: the translation for the domain:" + domain + " is: " + InetAddress.getByName(domain).getHostAddress()); 
            return InetAddress.getByName(domain);
        } catch (UnknownHostException ex) {            
            Logger.getLogger(DNSClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static InetAddress ninjaResolveDomain(String domain){
        DatagramSocket socketDNS = null;
        try {                        
            boolean received = false;
            socketDNS = new DatagramSocket();
            socketDNS.setSoTimeout (5000); // WE LIMIT THE WAIT TO 5SECS 
           
            
            while (!received) {                
                  sendQuery(domain,socketDNS, InetAddress.getByAddress(defaultAddr));
                  getResponse(socketDNS);
                  received = true;                
            }
            return respondAnswers.get(0);  
        } catch (UnknownHostException ex) {            
            Logger.getLogger(DNSClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SocketException ex) {
            Logger.getLogger(DNSClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DNSClient.class.getName()).log(Level.SEVERE, null, ex);
        } finally{            
            // increase the unique id
            uniqueID++;
            
            if(socketDNS != null){
                socketDNS.close ();
            }
             
        }
        return null;
    }
}
