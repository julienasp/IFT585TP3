
import java.util.Scanner;
import org.apache.log4j.Logger;


public class StartPoint {	
    //Private attribut for logging purposes
    private static final Logger logger = Logger.getLogger(StartPoint.class);    
	public static void main(String[] args) {
            
                
                
                

		Scanner sc = new Scanner(System.in);
                
                System.out.println("Quels algorithme de routage souhaitez-vous utiliser ? ");
                System.out.println("1 --- LS (Link-state) --- ");
                System.out.println("2 --- DV (Distance-vector) --- ");
                System.out.println("0 --- Aucun --- ");
                
                int choixUser = sc.nextInt();
		
                switch (choixUser) 
                {
                case 0:

                        sc.close();
                        break;

                case 1:
                                               
                        break;

                case 2: 
                                              
                        break;
                }
		
                
                
                
	}

}
