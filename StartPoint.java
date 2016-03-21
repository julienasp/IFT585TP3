
import client.HTTPClient;
import java.util.Scanner;
import org.apache.log4j.Logger;


public class StartPoint {	
    //Private attribut for logging purposes
    private static final Logger logger = Logger.getLogger(StartPoint.class);    
	public static void main(String[] args) {

		Scanner sc = new Scanner(System.in);                
                System.out.println("Veuillez saisir le URL complet du site internet que vous souhaitez récupérer ? Ex. http://stackoverflow.com/questions");             
                
                String choixUser = sc.nextLine();
                
                HTTPClient myHTTPClient = new HTTPClient();
                
                if( myHTTPClient.enableConnection(choixUser) == 1 ){
                    logger.info("StartPoint: La connexion a été établie");
                    myHTTPClient.executeRequest();
                }
                else{
                    logger.info("StartPoint: il semble y avoit un problème au niveau de la connexion");
                }
                
		
                
		
                
                
                
	}

}
