package client;
import java.io.IOException;
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
    public int enableConnection(String fullURL){        
        try {
            URI oURI = new URI(fullURL);
            distantHostName = oURI.getHost();
            distantHostName = distantHostName.startsWith("www.") ? distantHostName.substring(4) : distantHostName;
            distantHostURI = oURI.getPath();
        } catch (URISyntaxException ex) {
            Logger.getLogger(HTTPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    
        //We fetch the ip address with DNSClient
        InetAddress domainInetAdress = DNSClient.resolveDomain(distantHostName);        
        try {
            //Connection established 
            httpClientSocket = new Socket(domainInetAdress,80);
            return 1; // Connection established
        } catch (IOException ex) {
            Logger.getLogger(HTTPClient.class.getName()).log(Level.SEVERE, null, ex);
            return -1; // Connection not established
        }       
    }
    
    public String createRequestHeader(String domainURL){
        try {
            URI oURI = new URI(domainURL);
            String hostName = oURI.getHost();
            hostName = hostName.startsWith("www.") ? hostName.substring(4) : hostName;
            String header = "GET "+ distantHostURI + " HTTP/1.1" + "\r\n" 
                    + "Host: " + hostName + "\r\n" 
                    + "Connection: close" + "\r\n";
            return header;
        } catch (URISyntaxException ex) {
            Logger.getLogger(HTTPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public void executeRequest(){
    
        
        String request = createRequestHeader;
        OutputStream os = socket.getOutputStream();
        os.write(request.getBytes());
        os.flush();

        InputStream is = socket.getInputStream();
        int ch;
        while( (ch=is.read())!= -1)
            System.out.print((char)ch);
        socket.close();  
    }
}
