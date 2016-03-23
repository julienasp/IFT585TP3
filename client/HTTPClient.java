package client;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
    
    public void showImageListContent(){
        //Création d'un set pour parcourir la Hashtable
        Set set = this.getImageList().entrySet();

        //Création d'un iterator pour parcourir notre set
        Iterator it = set.iterator();

        //Boucle while qui parcours le set.
        while (it.hasNext()) {
          Map.Entry entry = (Map.Entry) it.next();
          logger.info("StartPoint: Nous avons l'image suivante: " + (String) entry.getKey());
          logger.info("StartPoint: et son url est le suivant: " + (String) entry.getValue());                  
        }
    }
     
    /**************************************/
    /*************   METHODS  *************/
    /**************************************/
    private String createRequestHeader(){
        
        logger.info("HTTPClient: createRequestHeader() is being execute!");
        
        //Formating the header
        String header = "GET "+ distantHostURI + " HTTP/1.1" + "\r\n" 
                    + "Host: " + distantHostName + "\r\n"
                    + "Cache-Control: no-cache" + "\r\n"
                    + "Connection: close" + "\r\n\r\n";
        
        logger.info("HTTPClient: createRequestHeader() return this header: " + header);
        
        return header;
    }
    
     private String createHeadRequestHeader(){
        
        logger.info("HTTPClient: createHeadRequestHeader() is being execute!");
        
        //Formating the header
        String header = "HEAD "+ distantHostURI + " HTTP/1.1" + "\r\n" 
                    + "Host: " + distantHostName + "\r\n"
                    + "Cache-Control: no-cache" + "\r\n"
                    + "Connection: close" + "\r\n\r\n";
        
        logger.info("HTTPClient: createHeadRequestHeader() return this header: " + header);
        
        return header;
    }
    
    private void extractImageFromURL(String source){
        logger.info("HTTPClient: extractImageFromURL() is being execute!");
        String patternForImageURL = "<img.*?src=\"(.*?)\".*?>";
        String patternForImageName = "\\/([a-zA-Z0-9-_]*?.(jpg|gif|tiff|jpeg))";
        Pattern patternForPath = Pattern.compile(patternForImageURL);
        Pattern patternForName = Pattern.compile(patternForImageName);
        Matcher matcherForPath = patternForPath.matcher(source);
               
        //we iterate through the string to find any matches
        while(matcherForPath.find()) {            
            String url = matcherForPath.group(1);
            logger.info("HTTPClient: extractImageFromURL() we found a match for the url: " + url);
            Matcher matcherForName = patternForName.matcher(url);
            if(matcherForName.find()){
                String name = matcherForName.group(1);
                logger.info("HTTPClient: extractImageFromURL() we found a match for the file name: " + name);
                //We add the img name and its url to the ImageList
                addImageURLToImageList(name,url);
                logger.info("HTTPClient: extractImageFromURL() the image name and its url were added to the list."); 
            }
        }
         
    }
    
    public int enableConnection(String domainURL){  
        
        logger.info("HTTPClient: enableConnection() is being execute!");
        logger.info("HTTPClient: enableConnection() : we need to connect to: " + domainURL);
        try {
            
            logger.info("HTTPClient: enableConnection() : we extract the information from the given URL");
            
            URI oURI = new URI(domainURL);
            distantHostName = oURI.getHost();
            distantHostName = distantHostName.startsWith("www.") ? distantHostName.substring(4) : distantHostName;            
            distantHostURI = oURI.getPath().isEmpty() ? "/index.html": oURI.getPath();            
            logger.info("HTTPClient: enableConnection() : distantHostName is: " + distantHostName);
            logger.info("HTTPClient: enableConnection() : distantHostURI is: " + distantHostURI);
        } catch (URISyntaxException ex) {
            Logger.getLogger(HTTPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    
        //We fetch the ip address with DNSClient
        InetAddress domainInetAdress = DNSClient.resolveDomain(distantHostName);
        logger.info("HTTPClient: enableConnection() : we try to resolve the domain IP with DNSClient.");
        try {
            logger.info("HTTPClient: enableConnection() : Waiting for connection.");
            //Connection established 
            httpClientSocket = new Socket(domainInetAdress,80); 
            logger.info("HTTPClient: enableConnection() : The connection has been established.");
            return 1; // Connection established
        } catch (IOException ex) {
            Logger.getLogger(HTTPClient.class.getName()).log(Level.SEVERE, null, ex);
            return -1; // Connection not established
        }       
    }
    
    
    
    public void executeRequest(){        
        try {
            byte[] buffer = new byte[1500];
            String request = createRequestHeader();
            OutputStream os = httpClientSocket.getOutputStream();
            os.write(request.getBytes());
            os.flush();
            
            InputStream is = httpClientSocket.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
                        
            while( (bis.read(buffer))!= -1){
                String tempString = new String(buffer);
                extractImageFromURL(tempString);
                logger.info("HTTPClient: executeRequest(): receving a byte of data");                
                logger.info("HTTPClient: executeRequest(): value of the byte of data is: " + tempString);
            }
            httpClientSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(HTTPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void executeRequestForImageDownload(String imageFileName){        
        try {            
            byte[] buffer = new byte[1024];  
            
            //We create the GET request header            
            String request = createRequestHeader();
            
            //We send the request to the server
            OutputStream os = httpClientSocket.getOutputStream();
            os.write(request.getBytes());
            os.flush();
            
            //We handle the respond
            InputStream is = httpClientSocket.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            FileOutputStream out = new FileOutputStream("./images/" + imageFileName, true);           
            
            String stringToMatch = "\r\n\r\n";
            StringBuilder sbTemp = new StringBuilder();
            boolean run = true;
            int ch;
            
            //WE LOOP UNTIL WE GET THE END OF THE HEADER
            while( run && (ch = bis.read())!= -1  ){                
                if((char)ch == '\r' && (sbTemp.length() == 0 || sbTemp.length() == 2 )){
                    sbTemp.append((char) ch);
                }
                else if((char)ch == '\n' && (sbTemp.length() == 1 || sbTemp.length() == 3 )){
                    sbTemp.append((char) ch);
                }
                else{
                    //WE RESET THE TEMPSTRING, BECAUSE WE'RE NOT AT THE END YET. 
                    sbTemp = null;
                    sbTemp = new StringBuilder();
                }
                if(stringToMatch.equals(sbTemp.toString())) run = false;                
            }
            /*
            //WE SKIPPED THE HTTP HEADER, NOW WE CAN USE THE BUFFER TO IMPROVE SPEED
            while( (bis.read(buffer))!= -1){
                //WE SAVE THE DATA INTO THE FILE                
                out.write(buffer);
                
                //WE EMPTY THE BUFFER
                buffer = null;
                buffer = new byte[1024];
            }*/
            while((ch = bis.read()) != -1){
                os.write(ch);
            }
            out.close();
            bis.close();
            is.close();
            httpClientSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(HTTPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public void downloadImages(){
        //Création d'un set pour parcourir la Hashtable
        Set set = this.getImageList().entrySet();

        //Création d'un iterator pour parcourir notre set
        Iterator it = set.iterator();

        //Boucle while qui parcours le set.
        while (it.hasNext()) {
          Map.Entry entry = (Map.Entry) it.next();
          enableConnection((String) entry.getValue());
          executeRequestForImageDownload((String) entry.getKey());
          logger.info("HTTPCLient: Nous avons l'image suivante: " + (String) entry.getKey());
          logger.info("HTTPClient: et son url est le suivant: " + (String) entry.getValue());                  
        }
    }
}
