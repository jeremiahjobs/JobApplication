package helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Observable;

import javax.swing.JOptionPane;

import model_bank.AKunde;
import model_bank.Bank;
import model_bank.Konto;

public class ObjectFileIO extends Observable{
	
	public ObjectFileIO() {
	}
	
	/**
	 * Writes the Konto hashmap that includes all Konto data for all customers as a object to an indicated location
	 * @param file = a file object that preferably comes from a JFileChooser
	 */
	public void writeObjectFile(File file) {
		
		try {
			HashMap<String, LinkedList<Konto>> kontoMap  = Bank.getInstance().getKontoMap();
	 	    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeObject(kontoMap); 
			oos.close();
		}catch(IOException e) {
			JOptionPane.showMessageDialog(null, "FEHLER! "+ file.getName() +" konnte nicht geschrieben werden");
			e.printStackTrace();
		}
	}
	
	/**
	 * reads an object file Konto hashmap that includes all Konto data for all customers from an indicated location
	 * @param file = a file object that preferably comes from a JFileChooser
	 */
	public void readObjectFile(File file) {

		try {
			ObjectInputStream oos = new ObjectInputStream(new FileInputStream(file.getName()));
			HashMap<String, LinkedList<Konto>> kontoMap = (HashMap<String, LinkedList<Konto>>) oos.readObject();
			assignAllKontenFromObjectFile(kontoMap);
			oos.close();
		}catch(IOException e) {
			JOptionPane.showMessageDialog(null, "FEHLER! "+ file.getName() +" konnte nicht gelesen werden");
		}catch(ClassNotFoundException e) {
			JOptionPane.showMessageDialog(null, "FEHLER! "+ file.getName() +" konnte nicht gefunden werden");
		}
		
	}
	
	/**
	 * Assigns all Konten that the afore saved object file included to the customer and then incorporates it into
	 * the kundenMap inside the class Bank using the assignKontoToKunde method. Now the GUI can display all Konten from the object file.
	 * @param kontoMap
	 */
	private void assignAllKontenFromObjectFile(HashMap<String, LinkedList<Konto>> kontoMap) {
		
		Bank postbank = Bank.getInstance();
		Konto kontoTmp = null; 
		AKunde kunde = null;
		
		try {
			for (Entry<String, LinkedList<Konto>> entry : kontoMap.entrySet()){
				String kundennummer = entry.getKey();
				kunde = postbank.getKundeByKundennr(kundennummer);
				for(Konto konto : entry.getValue()) {
						postbank.assignKontoToKunde(konto, kunde);
						kontoTmp = konto;
				}
			}
		} catch (NullPointerException e) {
			JOptionPane.showMessageDialog(null, "FEHLER! Konto " + kontoTmp + " konnte nicht zugewiesen werden");
			e.printStackTrace();
		} catch (KundenlisteOutOfBoundException e) {
			JOptionPane.showMessageDialog(null, "FEHLER! Maximale Kontoanzahl erreicht.");
			e.printStackTrace();
		};
	}
}
