package client;
import java.io.ByteArrayOutputStream;
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
    
    
    
    /*public static InetAddress resolveDomain(String domain){        
        try {
            logger.info("DNSClient: the translation for the domain:" + domain + " is: " + InetAddress.getByName(domain).getHostAddress()); 
            return InetAddress.getByName(domain);
        } catch (UnknownHostException ex) {            
            Logger.getLogger(DNSClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }*/
    public static InetAddress resolveDomain(String domain){        
        try {
            logger.info("DNSClient: the translation for the domain:" + domain + " is: " + InetAddress.getByName(domain).getHostAddress());             
            boolean received = false;
            DatagramSocket socketDNS = new DatagramSocket ();
            socketDNS.setSoTimeout (5000); // WE LIMIT THE WAIT TO 5SECS 
            
            String uniqueID = UUID.randomUUID().toString();
            while (!received) {                
                  sendQuery(domain, uniqueID,socketDNS, InetAddress.getByAddress(defaultAddr));
                  getResponse(socketDNS);
                  received = true;                
            } 
            
            socketDNS.close ();
        } catch (UnknownHostException ex) {            
            Logger.getLogger(DNSClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SocketException ex) {
            Logger.getLogger(DNSClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DNSClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static void sendQuery (String domainName, String uniqueID, DatagramSocket socketDNS, InetAddress nameServer) throws IOException {
        byte[] data = buildQuery(domainName,uniqueID);
        DatagramPacket packet = new DatagramPacket(data, data.length, nameServer, 53); // 53 is the default port
        socketDNS.send (packet);
      }

      public static void getResponse (DatagramSocket socketDNS) throws IOException {
        byte[] buffer = new byte[512];
        DatagramPacket packet = new DatagramPacket (buffer, buffer.length);
        socketDNS.receive (packet);
        query.receiveResponse (packet.getData (), packet.getLength ());
     }
      
    private static byte[] buildQuery(String domainName, String uniqueID){     
        ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream ();
        DataOutputStream dataOut = new DataOutputStream (byteArrayOut);
        try {
            dataOut.writeShort (Short.parseShort(uniqueID));
            dataOut.writeShort ( (1 << 7) |(1 << 8) ); // Recursion Desired and Recursion Available
            dataOut.writeShort (1); // nb of queries
            dataOut.writeShort (0); // nb of answers
            dataOut.writeShort (0); // nb of authorities
            dataOut.writeShort (0); // nb of additional
            StringTokenizer labels = new StringTokenizer (domainName, ".");
            while (labels.hasMoreTokens ()) {
              String label = labels.nextToken ();
              dataOut.writeByte (label.length ());
              dataOut.writeBytes (label);
            }
            dataOut.writeByte (0);
            dataOut.writeShort (255); // Request any
            dataOut.writeShort (1); // Class Internet
          } catch (IOException ex) {
              Logger.getLogger(DNSClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    return byteArrayOut.toByteArray ();
  }
    };
    
}
