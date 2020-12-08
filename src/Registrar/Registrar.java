package Registrar;


import Visitor.VisitorInterface;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import Cathering.CatheringInterface;



/*

The registrar has three major tasks.
First, it enrolls new catering facilities and provides them with a tool
to generate QR codes on a daily basis. Second, it enrolls new users
and provides them with tokens that can be used when visiting a
catering facility. Third, it reveals contact information of possibly
infected people. The matching service keeps information about
visits and supports contact tracing. Note that uniquely identifying
user and catering facility data are not revealed to the matching
service

Note that the registrar keeps the mapping between the phone number
and the tokens that were issued

 */

public class Registrar extends UnicastRemoteObject implements RegistrarInterface {
    /**
	 * 
	 */
	private static final long serialVersionUID = -7945048366389923378L;

	
	

	private static final int MAX_VISITS_ALLOWED = 3; // moet 48 zijn

    private List<VisitorInterface> visitors;
    private List <CatheringInterface> catherings;

    public static ObservableList<String> visitorNameNumber;
    public static ObservableList<String> catheringNameNumber;
    
    private static byte[] initializationVector;
    
    private static LocalDate lDate = LocalDate.now();
    
    //private static final String s = "boooooooooom!!!!";
    private static final String salt = "ssshhhhhhhhhhh!!!!";
    
    private String s;

    private Map<String, List<byte[]>> userTokens; //key=phonenr || value=visitorTokens
    private Map<String, LocalDate> dateGeneratedTokens; // wanneer tokens laatste keer gegenereerd

    public Registrar() throws RemoteException {
        visitors = new ArrayList<>();
        catherings = new ArrayList<>();
        visitorNameNumber = FXCollections.observableArrayList();
        catheringNameNumber = FXCollections.observableArrayList();
        dateGeneratedTokens = new HashMap<>();
        userTokens = new HashMap<>();
        //initializationVector = createInitializationVector();
        
        
        try{
        	createAESKey();
        	System.out.println("Secret key: " + s);
        }catch(Exception e) {
        	e.printStackTrace();
        }
        //System.out.println("The Secret Key is :" + s); 
    }

    @Override
    public boolean newVisitor(VisitorInterface vi) throws RemoteException {

        Platform.runLater(() -> {
            try {
                visitors.add(vi);
                visitorNameNumber.add(vi.getName() + "[" +vi.getNumber() + "]");
                System.out.println("New user: " + vi.getName());
                List<byte[]> tokens = new ArrayList<>();
        		for(int i=0;i<48;i++) {
        			byte[] token = generateToken();
            		tokens.add(token);
            		userTokens.get(vi.getNumber()).add(token);
        		} 
        		vi.receiveTokens(tokens);

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
        return true;
    }

    @Override
    public boolean newCathering(CatheringInterface ci) throws RemoteException {

        Platform.runLater(() -> {
            try {
                catherings.add(ci);
                catheringNameNumber.add(ci.getCatheringName() + "[" +ci.getBusinnessNumber() + "]");
                System.out.println("New cathering facility: " + ci.getCatheringName());
                byte[] nym = generateDailyPseudonym(ci.getBusinnessNumber(), ci.getLocation());
        		ci.receivePseudonym(nym);

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
        return true;
    }

    @Override
    public boolean checkCatheringInformation(String businessNumber) throws RemoteException {
        for (CatheringInterface ci: catherings) {
            if (ci.getBusinnessNumber().equals(businessNumber)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean checkUserInformation(String number) throws RemoteException {
        for (VisitorInterface visitor: visitors) {
            if (number.equals(visitor.getNumber())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<String> getTokens(String number) throws RemoteException {
        LocalDate today = LocalDate.now();

        LocalDate lastUpdate;
        if (dateGeneratedTokens.containsKey(number)) {
            lastUpdate = dateGeneratedTokens.get(number);
            if (today.isBefore(lastUpdate) || today.isEqual(lastUpdate)) {
                return null;
            }
        }

        dateGeneratedTokens.replace(number, today);
        List<String> newTokens = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < MAX_VISITS_ALLOWED; i++) {
            newTokens.add(String.valueOf(random.nextInt(10000000)));
        }
        return newTokens;
    }

    @Override
    public VisitorInterface getVisitor(String number) throws RemoteException{
        for(VisitorInterface visitorInterface : visitors) {
            if(visitorInterface.getNumber().equals(number)) {
                return visitorInterface;
            }
        }
        return null;
    }

    @Override
    public void disconnectVisitor(VisitorInterface vi) throws RemoteException {
        try {
            String info = vi.getName() + "[" +vi.getNumber() + "]";
            visitorNameNumber.removeAll(info);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        visitors.remove(vi);

    }

    @Override
    public void disconnectCathering(CatheringInterface ci) throws RemoteException {
        try {
            catheringNameNumber.removeAll(ci.getCatheringName() + "[" +ci.getBusinnessNumber() + "]");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        catherings.remove(ci);
    }

    @Override
    public byte[] generateDailyPseudonym(String businnessNumber, String location) throws RemoteException {
    	// mm/dd/yyyy
    	String date = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).format(lDate);
    	System.out.println("Date: " + date);
    	String plainText = businnessNumber + ";" + date;
    			
    	
    // Encrypting the message 
    // using the symmetric key 
    	byte[] cipherText = null;
    	try {
        	cipherText = do_AESEncryption(plainText, s);  //Represents S(CF,day)
        	//System.out.println("Key gen: " + DatatypeConverter.printHexBinary(cipherText));
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    	MessageDigest md;
    	byte[] nym = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
			String strToHash = location + ";" + date;
			md.update(cipherText); //cipherText is used as salt
			nym = md.digest(strToHash.getBytes(StandardCharsets.UTF_8));
			//System.out.println("Nym gen: " + DatatypeConverter.printHexBinary(nym));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
    	return nym;        
    }
    public void newDay() throws RemoteException{
    	lDate = lDate.plus(1, ChronoUnit.DAYS);
    	for(CatheringInterface ci : catherings) {
    		byte[] nym = generateDailyPseudonym(ci.getBusinnessNumber(), ci.getLocation());
    		ci.receivePseudonym(nym);
    	}
    	for(VisitorInterface vi : visitors) {
    		List<byte[]> tokens = new ArrayList<>();
    		for(int i=0;i<48;i++) {
    			byte[] token = generateToken();
        		tokens.add(token);
        		userTokens.get(vi.getNumber()).add(token);
    		} 
    		vi.receiveTokens(tokens);
    	}
    	
    }
 // Function to create a secret key 
    public void createAESKey() throws Exception { 
  
        // Creating a new instance of 
        // SecureRandom class. 
        SecureRandom securerandom = new SecureRandom(); 
  
        // Passing the string to KeyGenerator 
        KeyGenerator keygenerator = KeyGenerator.getInstance("AES"); 
        
        // Initializing the KeyGenerator with 256 bits. 
        keygenerator.init(256, securerandom); 
        SecretKey key = keygenerator.generateKey(); 
        s = DatatypeConverter.printHexBinary(key.getEncoded()); 
    } 
    
    public static byte[] do_AESEncryption(String plainText, String secret) throws Exception { 
    	byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        IvParameterSpec ivspec = new IvParameterSpec(iv);
         
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		KeySpec spec = new PBEKeySpec(secret.toCharArray(), salt.getBytes(), 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING"); 
        
        //System.out.println(DatatypeConverter.printHexBinary(secretKey.getEncoded()));
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);   
        return cipher.doFinal(plainText.getBytes()); 
    } 
 // This function performs the 
    // reverse operation of the 
    // do_AESEncryption function. 
    // It converts ciphertext to 
    // the plaintext using the key. 
    public static String do_AESDecryption(byte[] cipherText, SecretKey secretKey, 
    		byte[] initializationVector) throws Exception {
    	Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING"); 
  
        IvParameterSpec ivParameterSpec = new IvParameterSpec(initializationVector); 
        //System.out.println("InitVector: " + DatatypeConverter.printHexBinary(initializationVector));
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec); 
  
        byte[] result = cipher.doFinal(cipherText); 
  
        return new String(result); 
    }
    public static byte[] generateToken() {
    	byte[] token = new byte[16]; 
        SecureRandom secureRandom = new SecureRandom(); 
        secureRandom.nextBytes(token); 
        return token; 
    }

	@Override
	public void informCathering(String datetime, String CF) throws RemoteException{
		for(CatheringInterface ci : catherings) {
			if(ci.getBusinnessNumber().equals(CF)) {
				String s = "There was an infected visitor in your business [" + datetime + "]";
				//ci.receiveMessage(s);
			}
		}
    	
		
	}

	@Override
	public byte[] getPseudonym(String CF, String date) throws RemoteException {
		CatheringInterface ci = null;
    	for(CatheringInterface c : catherings) {
    		if(c.getBusinnessNumber().equals(CF)) {
    			ci = c;
    			break;
    		}
    	}
    	
		byte[] cipherText = null;
		String plaintext = ci.getBusinnessNumber() + ";" + date;
    	try {
        	cipherText = do_AESEncryption(plaintext, s);
        	System.out.println("controle key: " + DatatypeConverter.printHexBinary(cipherText));
    	}catch(Exception e) {
    		e.printStackTrace();
    	}    	
    	MessageDigest md;
    	byte[] nym = null;
    	try {
			md = MessageDigest.getInstance("SHA-256");
			String strToHash = ci.getLocation() + ";" + date;
			md.update(cipherText); //cipherText is used as salt
			nym = md.digest(strToHash.getBytes(StandardCharsets.UTF_8));
			System.out.println("Nym control: " + DatatypeConverter.printHexBinary(nym));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
    	return nym;
	}
	public static byte[] createInitializationVector() 
    { 
  
        // Used with encryption 
        byte[] initializationVector 
            = new byte[16]; 
        SecureRandom secureRandom 
            = new SecureRandom(); 
        secureRandom.nextBytes(initializationVector); 
        System.out.println(DatatypeConverter.printHexBinary(initializationVector));
        return initializationVector; 
    }

	@Override
	public LocalDate getDate() throws RemoteException {
		return lDate;
	}

}
