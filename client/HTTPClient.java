package client;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;
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
    /**************************************/
    /********* PRIVATE ATTRIBUTS **********/
    /**************************************/ 
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(HTTPClient.class);  
    private Socket httpClientSocket = null;
    private String distantHostName = null;
    private String distantHostURI = null;
    private Hashtable<String, String> imageList = new Hashtable<String,String>();

    
    /**************************************/
    /********* GETTER AND SETTER **********/
    /**************************************/
    public Socket getHttpClientSocket() {
        return httpClientSocket;
    }

    public void setHttpClientSocket(Socket httpClientSocket) {
        this.httpClientSocket = httpClientSocket;
    }

    public Hashtable<String, String> getImageList() {
        return imageList;
    }

    public void setImageList(Hashtable<String, String> imageList) {
        this.imageList = imageList;
    }
    
    
    
    
    /**************************************/
    /********   UTILITY METHODS  **********/
    /**************************************/
    public void addImageURLToImageList(String imageName,String imageURL) {
       imageList.put(imageName, imageURL);
    }
    
    public void removeImageURLFromImageList(String imageName,String imageURL) {
       imageList.remove(imageName);
    }
     
    /**************************************/
    /*************   METHODS  *************/
    /**************************************/
    private String createRequestHeader(){
        
        logger.info("HTTPClient: createRequestHeader() is being execute!");
        
        //Formating the header
        String header = "GET "+ distantHostURI + " HTTP/1.1" + "\r\n" 
                    + "Host: " + distantHostName + "\r\n" 
                    + "Connection: close" + "\r\n";
        
        logger.info("HTTPClient: createRequestHeader() return this header: " + header);
        
        return header;
    }
    public int enableConnection(String domainURL){  
        
        logger.info("HTTPClient: enableConnection() is being execute!");
        logger.info("HTTPClient: enableConnection() : we need to connect to: " + domainURL);
        try {
            
            logger.info("HTTPClient: enableConnection() : we extract the information from the given URL");
            
            URI oURI = new URI(domainURL);
            distantHostName = oURI.getHost();
            distantHostName = distantHostName.startsWith("www.") ? distantHostName.substring(4) : distantHostName;            
            distantHostURI = oURI.getPath();
            logger.info("HTTPClient: enableConnection() : distantHostName is: " + distantHostName);
            logger.info("HTTPClient: enableConnection() : distantHostURI is: " + distantHostURI);
        } catch (URISyntaxException ex) {
            Logger.getLogger(HTTPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    
        //We fetch the ip address with DNSClient
        InetAddress domainInetAdress = DNSClient.resolveDomain(distantHostName);
        logger.info("HTTPClient: enableConnection() : we try to resolve the domain IP with DNSClient.");
        try {
            //Connection established 
            httpClientSocket = new Socket(domainInetAdress,80);
            
            logger.info("HTTPClient: enableConnection() : the connection is established.");
            return 1; // Connection established
        } catch (IOException ex) {
            Logger.getLogger(HTTPClient.class.getName()).log(Level.SEVERE, null, ex);
            return -1; // Connection not established
        }       
    }
    
 
    
    public void executeRequest(){        
        try {
            String request = createRequestHeader();
            OutputStream os = httpClientSocket.getOutputStream();
            os.write(request.getBytes());
            os.flush();
            
            InputStream is = httpClientSocket.getInputStream();
            int ch;
            while( (ch=is.read())!= -1)
                logger.info((char)ch);  
            httpClientSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(HTTPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
