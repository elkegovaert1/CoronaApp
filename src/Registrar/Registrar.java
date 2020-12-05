package Registrar;

import Cathering.CatheringInterface;
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
import java.time.LocalDate;
import java.util.*;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

//import javax.xml.bind.DatatypeConverter; 


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
    private static final int MAX_VISITS_ALLOWED = 3; // moet 48 zijn

    private List<VisitorInterface> visitors;
    private List <CatheringInterface> catherings;

    public static ObservableList<String> visitorNameNumber;
    public static ObservableList<String> catheringNameNumber;
    
    private static SecretKey s;

    private Map<String, List<String>> userTokens; //key=phonenr || value=visitorTokens
    private Map<String, LocalDate> dateGeneratedTokens; // wanneer tokens laatste keer gegenereerd

    public Registrar() throws RemoteException {
        visitors = new ArrayList<>();
        catherings = new ArrayList<>();
        visitorNameNumber = FXCollections.observableArrayList();
        catheringNameNumber = FXCollections.observableArrayList();
        dateGeneratedTokens = new HashMap<>();
        userTokens = new HashMap<>();
        try{
        	s = createAESKey();
        }catch(Exception e) {
        	e.printStackTrace();
        }
        //System.out.println("The Secret Key is :" + DatatypeConverter.printHexBinary( s.getEncoded())); 
        //IK KAN DEZE PACKAGE (datatypeconverter) NIET IMPORTEREN, JIJ?
    }

    @Override
    public boolean newVisitor(VisitorInterface vi) throws RemoteException {

        Platform.runLater(() -> {
            try {
                visitors.add(vi);
                visitorNameNumber.add(vi.getName() + "[" +vi.getNumber() + "]");
                System.out.println("New user: " + vi.getName());

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
    	String plainText = businnessNumber + ";" + LocalDate.now().toString(); 

    // Encrypting the message 
    // using the symmetric key 
    	byte[] cipherText = null;
    	try {
        	cipherText = do_AESEncryption(plainText, s, createInitializationVector()); 
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    	MessageDigest digest;
    	byte[] nym = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
			String strToHash = cipherText.toString() + ";" + location + ";" + LocalDate.now().toString();
			nym = digest.digest(strToHash.getBytes(StandardCharsets.UTF_8));
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return nym;        
    }
    public void newDay() {
    	for(CatheringInterface ci : catherings) {
    		byte[] nym = generateDailyPseudonym(ci.getBusinnessNumber(), ci.getLocation());
    		ci.receivePseudonym(nym);
    	}
    	for(VisitorInterface vi : visitors) {
    		for(int i=0;i<48;i++) {
    			String token = "generateToken";
        		vi.receiveToken(token);
        		userTokens.get(vi.getNumber()).add(token);
    		}    		    		
    	}
    }
 // Function to create a secret key 
    public static SecretKey createAESKey() throws Exception { 
  
        // Creating a new instance of 
        // SecureRandom class. 
        SecureRandom securerandom = new SecureRandom(); 
  
        // Passing the string to 
        // KeyGenerator 
        KeyGenerator keygenerator = KeyGenerator.getInstance("AES"); 
  
        // Initializing the KeyGenerator 
        // with 256 bits. 
        keygenerator.init(256, securerandom); 
        SecretKey key = keygenerator.generateKey(); 
        return key; 
    } 
 // Function to initialize a vector 
    // with an arbitrary value 
    public static byte[] createInitializationVector() {   
        // Used with encryption 
        byte[] initializationVector = new byte[16]; 
        SecureRandom secureRandom = new SecureRandom(); 
        secureRandom.nextBytes(initializationVector); 
        return initializationVector; 
    }
 // This function takes plaintext, 
    // the key with an initialization 
    // vector to convert plainText 
    // into CipherText. 
    public static byte[] do_AESEncryption(String plainText, SecretKey secretKey, 
    		byte[] initializationVector) throws Exception { 
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING"); 
  
        IvParameterSpec ivParameterSpec = new IvParameterSpec(initializationVector); 
  
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec); 
  
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
  
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec); 
  
        byte[] result = cipher.doFinal(cipherText); 
  
        return new String(result); 
    }
}
