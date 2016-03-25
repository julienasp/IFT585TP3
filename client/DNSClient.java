package client;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
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
            int count = 0;
            DatagramSocket socketDNS = new DatagramSocket ();
            socketDNS.setSoTimeout (5000);
            
            while (!received) {                
                  sendQuery(domain, socketDNS, InetAddress.getByAddress(defaultAddr));
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

    public static void sendQuery (String domainName, DatagramSocket socketDNS, InetAddress nameServer) throws IOException {
        byte[] data = buildQuery(domainName);
        DatagramPacket packet = new DatagramPacket(data, data.length, nameServer, 53); // 53 is the default port
        socketDNS.send (packet);
      }

      public static void getResponse (DatagramSocket socketDNS) throws IOException {
        byte[] buffer = new byte[512];
        DatagramPacket packet = new DatagramPacket (buffer, buffer.length);
        socketDNS.receive (packet);
        query.receiveResponse (packet.getData (), packet.getLength ());
      }
    
}
