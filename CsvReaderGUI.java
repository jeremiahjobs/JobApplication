package helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

import javax.swing.JOptionPane;

import model_bank.Adresse;
import model_bank.Bank;
import model_bank.Firmenkunde;
import model_bank.Privatkunde;

public class CsvReaderGUI extends Observable {
	
	BufferedReader in = null;
	Bank postbank = Bank.getInstance();
		
	/**
	 * Constructor 
	 */
	public CsvReaderGUI() {
	}
	
	/**
	 * Opens, reads and closes a stream for a file specified by the file name 
	 * @param name: name of the file to be read
	 */
	public void readCsvRowwise(File file) {
	
		openStream(file);
		readStream(file);
		closeStream();
	}
	
	/**
	 * Opens a FileInPutStream in UTF-8 encoding for the file specified by name.
	 * Only used in readCsvRowwise().
	 * @param name: name of the file to be read
	 */
	private void openStream(File file) {
		
			
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(file) , "UTF-8"));
		}catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog( null, "FEHLER! " + file.getName() + " konnte nicht geöffnet werden.");
			e.printStackTrace();
		}catch(UnsupportedEncodingException e) {
			JOptionPane.showMessageDialog( null, "FEHLER! Falsches Encoding!");
			e.printStackTrace();
		}catch(Exception e) {
			JOptionPane.showMessageDialog( null, "FEHLER!");
		}
	}
	
	/**Read stream and file that has been opened in openStream() add every line, that is, Kunde to the kundenMap 
	 * using the addKundeFromCSVToKundenMap method
	 * Only used in readCsvRowwise().
	 * @param name: name of the file to be read
	 * @return: returns list of row-wise written values out of the file that was read
	 */
	private LinkedList<String> readStream(File file) {
		
		String line = "";
		LinkedList<String> listtmp = new LinkedList<String>();
	
		try {
			while((line = in.readLine()) != null ){
				listtmp.add(line);
				this.addKundeFromCSVToKundenMap(line);
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog( null, "FEHLER! " + file.getName() + " konnte nicht gelesen werden.");
			e.printStackTrace();
		}
		return listtmp;
	}
	
	/**
	 * Closes Stream. Only used in readCsvRowwise().
	 */
	private void closeStream() {
		try {
			in.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog( null, "FEHLER! Stream konnte nicht geschlossen werden.");
		}
		setObservable();
	}
	
	/**
	 * Takes a comma seperated string, splits it after every comma and uses every value to creat a new customer
	 * @param line = comma seperated string that includes all customer data
	 */
	private void addKundeFromCSVToKundenMap(String line) {
		
		List<String> kundeParameterliste = Arrays.asList(line.split(","));
		Privatkunde privatkunde = null;
		Firmenkunde firmenkunde = null;
		Adresse adresse = null;
		
		try {
			adresse =new Adresse(kundeParameterliste.get(6), kundeParameterliste.get(7), kundeParameterliste.get(8), kundeParameterliste.get(9));
			
			if(kundeParameterliste.get(0).equals("P")) {				
				privatkunde = new Privatkunde(kundeParameterliste.get(1), kundeParameterliste.get(2), kundeParameterliste.get(3), 
							  adresse, kundeParameterliste.get(4), kundeParameterliste.get(5), kundeParameterliste.get(10));
				postbank.addKundeToKundenMap(privatkunde);
			}else{
				firmenkunde = new Firmenkunde(kundeParameterliste.get(1), kundeParameterliste.get(2), kundeParameterliste.get(3), 
							  adresse, kundeParameterliste.get(4), kundeParameterliste.get(5));
				postbank.addKundeToKundenMap(firmenkunde);
			}
		}catch(Exception e) {
			JOptionPane.showMessageDialog( null, "FEHLER! Datei enthält fehlerhafte Daten.");
		}		
	}
	
	/**
	 * Notifies GUI observer to update the JTable with all the Kunden data
	 */
	public void setObservable() {
		setChanged();
		notifyObservers();
		clearChanged();
	}
}
