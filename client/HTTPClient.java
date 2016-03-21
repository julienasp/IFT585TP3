package client;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
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
public class HTTPClient {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(HTTPClient.class);  
    private Socket httpClientSocket = null;
    public void enableConnection(String domain){
        InetAddress domainInetAdress = DNSClient.resolveDomain(domain);
        
        try {
            httpClientSocket = new Socket(domainInetAdress,80);
        } catch (IOException ex) {
            Logger.getLogger(HTTPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
                
                
    }
    
}
