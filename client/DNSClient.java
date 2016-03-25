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
            dataOut.writeShort((1 << 8)); // Recursion Desired and Recursion Available
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
    
    private static String readName(DataInputStream dis){
        try {
            boolean endOfName = false;
            String name = "";
            while(!endOfName){
                int nbByteToRead = dis.readByte();
                logger.info("DNSClient: readName(): Question nb of byte required for QNAME: " + nbByteToRead);
                if(nbByteToRead==0){
                    endOfName = true;
                    logger.info("DNSClient: readName(): the end of name marker was found!");
                }
                else{                
                    byte[] len = new byte[nbByteToRead];
                    dis.readFully(len);
                    String temp = new String(len);
                    logger.info("DNSClient: readName(): We found the name: " + temp);
                    name = (name.length() == 0) ? temp: name + "." + temp;
                }
            }
            return name;
        } catch (IOException ex) {
            Logger.getLogger(DNSClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
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
                
                String qname = readName(dis);
                logger.info("DNSClient: handleResponse(): Question QNAME: " + qname);
                
                String qname2 = readName(dis);
                logger.info("DNSClient: handleResponse(): Question QNAME: " + qname2);
                
                int qtype = dis.readUnsignedShort();
                logger.info("DNSClient: handleResponse(): Question QTYPE: " + qtype);
                
                int qclass = dis.readUnsignedShort();
                logger.info("DNSClient: handleResponse(): Question QCLASS: " + qclass);
            }
        }
        
        respondAnswers = Collections.synchronizedList(new ArrayList(nbAnswers));
        
        for (int i = 0; i < nbAnswers; i++){
            int nbByteAnswer = dis.readByte();
            logger.info("DNSClient: handleResponse(): Question nb of byte required for NAME: " + nbByteAnswer);
            byte[] lenString = new byte[nbByteAnswer];
            dis.readFully(lenString);
            String name = new String(lenString);            
            logger.info("DNSClient: handleResponse(): Answer Name: " + name);
            int type = dis.readUnsignedShort();
            logger.info("DNSClient: handleResponse(): Answer Type: " + type);
            int dnsClass = dis.readUnsignedShort();
            logger.info("DNSClient: handleResponse(): Answer dnsClass: " + dnsClass);
            long ttl = (dis.readInt() & 0xffffffffL); // read unsigned int needed
            logger.info("DNSClient: handleResponse(): Answer ttl: " + ttl);
            int len = dis.readUnsignedShort();
            logger.info("DNSClient: handleResponse(): Answer len: " + len);
            
            int end = offset + len;
            
            if(type == 1){ // TYPE A
                logger.info("DNSClient: handleResponse(): We add the answer in the answer list");
                byte[] adrTypeA = new byte[len];
                logger.info("DNSClient: handleResponse(): The offset is: "  + offset); 
                System.arraycopy(data, offset, adrTypeA, 0, len); 
                //logger.info("DNSClient: handleResponse(): the ip address: " + InetAddress.getByAddress(adrTypeA).getHostAddress() +"was added to the list");                
                //respondAnswers.add(InetAddress.getByAddress(adrTypeA));
            }
            offset = end;
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
