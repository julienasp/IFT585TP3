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
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(DNSClient.class);  

    public DNSClient() {       
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
        ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream ();
        DataOutputStream dataOut = new DataOutputStream (byteArrayOut);
        try {
            dataOut.writeShort(uniqueID);
            dataOut.writeShort( (1 << 7) |(1 << 8) ); // Recursion Desired and Recursion Available
            dataOut.writeShort(1); // nb of queries
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
    /*private static void handleResponse (byte[] data, int length) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream (data, 0, length);
        DataInputStream dis = new DataInputStream(bais);
        int id = dis.readUnsignedShort();
        if (id != Short.parseShort(uniqueID));
          throw new IOException ("ID does not match request");
        int flags = dis.readUnsignedShort();        
        int nbQueries = dis.readUnsignedShort(); 
        int nbAnswers = dis.readUnsignedShort(); 
        int nbAuthorities = dis.readUnsignedShort(); 
        int nbAdditional = dis.readUnsignedShort(); 
        while (nbQueries -- > 0) { // discard questions
          dnsIn.readDomainName ();
          dis.readUnsignedShort();
          dis.readUnsignedShort();
        }
        try {
          while (numAnswers -- > 0){
            answers.addElement (dnsIn.readRR ());
          }
          while (numAuthorities -- > 0)
            authorities.addElement (dnsIn.readRR ());
          while (numAdditional -- > 0)
            additional.addElement (dnsIn.readRR ());
        } catch (EOFException ex) {
          if (!truncated)
            throw ex;
        }
    }*/
 
    private static void sendQuery (String domainName, DatagramSocket socketDNS, InetAddress nameServer) throws IOException {
        byte[] data = buildQuery(domainName);
        DatagramPacket packet = new DatagramPacket(data, data.length, nameServer, 53); // 53 is the default port
        socketDNS.send(packet);
    }
    
    private static void getResponse (DatagramSocket socketDNS) throws IOException {
        byte[] buffer = new byte[512]; // MAX SIZE UDP PACKET
        DatagramPacket packet = new DatagramPacket (buffer, buffer.length);
        socketDNS.receive(packet);
        logger.info("DNSClient: getResponse():" + new String(packet.getData())); 
        //handleResponse(uniqueID, packet.getData(), packet.getLength());
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
