/**
 * @author: jeremiah dominguez gorrin
 * Email: jeremiah.dom.go@gmail.com
 * Version:Eclpise Neon3.Release 4.6.3
 * Description: Kundennummerngenerator
 * @since: 30.10.2017
 */

package helper;

import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * If the bank decides to create a new customer account the KundennrGenerator generates a randomized Kundennummer for this customer
 * @author Jeremiah 
 *
 */
public class KundennrGenerator {
	
	private LinkedList<String> kundennummerListe = new LinkedList<String>();;
	private static KundennrGenerator instance;
	
	private KundennrGenerator(){	
	}
	
	/**
	 * Singleton-Pattern for KundennrGenerator
	 * @return instance of the KundennrGenerator
	 */
	public static KundennrGenerator getInstance() {
		
		if(instance == null) {
			instance = new KundennrGenerator();
		}
		return instance;
	}
	
	/**
	 * generates a randomized Kundennummer with the pattern f.i. "TB-234-42"
	 * @return kundennummer
	 */
	public String generateKundennr(){
		
		String kundennummer;
		String teilEins;
		String teilZweiString;
		String teilDreiString;
		int teilZweiInt;
		int teilDreiInt;
		char buchstabeEins;
		char buchstabeZwei;
		boolean kundennummerOk = false;
		
		do{
			buchstabeEins = (char) ThreadLocalRandom.current().nextInt(65,91);
			buchstabeZwei = (char) ThreadLocalRandom.current().nextInt(65,91);
			teilEins = Character.toString(buchstabeEins) + Character.toString(buchstabeZwei);
			teilZweiInt = ThreadLocalRandom.current().nextInt(100,1000);
			teilZweiString = Integer.toString(teilZweiInt);
			teilDreiInt = ThreadLocalRandom.current().nextInt(10,100);
			teilDreiString = Integer.toString(teilDreiInt);
			kundennummer = teilEins + "-" + teilZweiString + "-" + teilDreiString;
			kundennummerOk = this.isKundennummerUnique(kundennummer);
			
			if(kundennummerOk == true) {
				this.addKundennummer(kundennummer);
			}

		}while(kundennummerOk == false);
		
		return kundennummer;
	}
	
	/**
	 * add the kunndennummer to the Kundennummerliste
	 * @param kundennummer
	 */
	private void addKundennummer(String kundennummer){
		kundennummerListe.add(kundennummer);	
	}
	
	/**
	 * checks whether the Kundennummer is unique
	 * @param kundennummer
	 * @return kundennummerOk; true if kundennummer is unique or false if it isn't
	 */
	private boolean isKundennummerUnique( String kundennummer){
		
		boolean kundennummerOk = false;

		for(int i = 0; i <= kundennummerListe.size()-1; i++){
			kundennummerListe.get(i);
			for (int j = 0; j <= kundennummerListe.get(i).length()-1; j++){
				if(kundennummer.charAt(j) != kundennummerListe.get(i).charAt(j)){
					kundennummerOk = true;
				}
			}	
		}	
		return kundennummerOk;	
	}
}
