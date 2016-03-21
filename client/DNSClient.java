package client;
import java.net.InetAddress;
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
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(DNSClient.class);  
    public static InetAddress resolveDomain(String domain){        
        try {
            logger.info("DNSClient: the translation for the domain:" + domain + " is :" + InetAddress.getByName(domain).getAddress().toString()); 
            return InetAddress.getByName(domain);
        } catch (UnknownHostException ex) {            
            Logger.getLogger(DNSClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
