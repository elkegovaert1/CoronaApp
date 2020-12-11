package Registrar;


import Visitor.VisitorInterface;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
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


public class Registrar extends UnicastRemoteObject implements RegistrarInterface {
	private static final long serialVersionUID = -7945048366389923378L;

    private List<VisitorInterface> visitors;
    private List <CatheringInterface> catherings;

    public static ObservableList<String> visitorNameNumber;
    public static ObservableList<String> catheringNameNumber;

    private static LocalDate lDate = LocalDate.now();
    
    private static final String salt = "ssshhhhhhhhhhh!!!!";
    
    private String s;
    
    private PrivateKey sk;
    private PublicKey pk;

    private Map<String, List<byte[]>> userTokens; //key=phonenr || value=visitorTokens
    private Map<String, List<byte[]>> signedTokens;
    private Map<String, LocalDate> dateGeneratedTokens; // wanneer tokens laatste keer gegenereerd

    public Registrar() throws RemoteException {
        visitors = new ArrayList<>();
        catherings = new ArrayList<>();
        visitorNameNumber = FXCollections.observableArrayList();
        catheringNameNumber = FXCollections.observableArrayList();
        dateGeneratedTokens = new HashMap<>();
        userTokens = new HashMap<>();
        signedTokens = new HashMap<>();
        KeyPair keypair;
		try {
			keypair = generateRSAKkeyPair();
			sk = keypair.getPrivate();
	        pk = keypair.getPublic();
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        try{
        	createAESKey();
        	System.out.println("Secret key: " + s);
        }catch(Exception e) {
        	e.printStackTrace();
        }
    }

    @Override
    public boolean newVisitor(VisitorInterface vi) throws RemoteException {

        Platform.runLater(() -> {
            try {
                visitors.add(vi);
                visitorNameNumber.add(vi.getName() + "[" +vi.getNumber() + "]");
                System.out.println("New user: " + vi.getName());
                List<byte[]> tokens = new ArrayList<>();
        		for(int i=0;i<24;i++) {
        			byte[] token = generateToken();
            		tokens.add(token);            		
        		} 
        		userTokens.put(vi.getNumber(), tokens);
        		signedTokens.put(vi.getNumber(), new ArrayList<>());
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
    		userTokens.get(vi.getNumber()).clear();
    		for(int i=0;i<24;i++) {
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
    	byte[] token = new byte[64]; 
        SecureRandom secureRandom = new SecureRandom(); 
        secureRandom.nextBytes(token); 
        return token; 
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
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
    	return nym;
	}
	public static byte[] createInitializationVector() {
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

	public static KeyPair generateRSAKkeyPair() throws Exception{
        SecureRandom secureRandom = new SecureRandom();
 
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
 
        keyPairGenerator.initialize(512, secureRandom);
 
        return keyPairGenerator.generateKeyPair();
	}

	@Override
	public void flush() throws RemoteException{
		for(VisitorInterface vi : visitors) {
			vi.didNotExitCathering();
		}
	}

	@Override
	public boolean checkToken(byte[] visitorToken, byte[] signature, PublicKey key) throws RemoteException {
		boolean foundToken = false;
		String mapKey = null;
		System.out.println("mapSize: " + userTokens.size());
		for(String phoneNumber : userTokens.keySet()) {
			System.out.println(phoneNumber + " size: " + userTokens.get(phoneNumber).size());
			for(byte[] token : userTokens.get(phoneNumber)) {
				if(Arrays.equals(token, visitorToken)) {
					foundToken = true;
					mapKey = phoneNumber;
					break;
				}
			}
			if(foundToken) {
				break;
			}
		}
		if(!foundToken) {
			System.out.println("visitorToken niet gevonden");
			return false;
		}else {
			try {
				if(Verify_Digital_Signature(visitorToken, signature, key)){
					userTokens.get(mapKey).remove(visitorToken); //Origineel token wordt verwjderd
					if(signedTokens.containsKey(mapKey)) { //Nieuwe entry aanmaken
						List<byte[]> signingTokens = new ArrayList<>();
						signingTokens.add(signature);
						signedTokens.put(mapKey, signingTokens);
					}else {
						signedTokens.get(mapKey).add(signature); //signedToken toevoegen aan bestaande lijst
					}
					return true;
				}else {
					System.out.println("Signed visitorToken komt niet overeen");
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		
	}

	public static boolean Verify_Digital_Signature(byte[] input, byte[] signatureToVerify, PublicKey key) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA"); 
        signature.initVerify(key); 
        signature.update(input); 
        return signature.verify(signatureToVerify); 
    }

	@Override
	public void warnVisitor(byte[] visitorToken, String date, String phone) throws RemoteException {
		for(String number : signedTokens.keySet()) {
			for(byte[] signedToken : signedTokens.get(number)) {
				if(Arrays.equals(visitorToken, signedToken)) {
					for(VisitorInterface vi : visitors) {
						if(vi.getNumber().equals(number) && !vi.getNumber().equals(phone)) {
							vi.receiveMessage("You came close to an infected person [" + date + "]");
						}
					}
				}
			}
		}
		
	}

	@Override
	public void warnCathering(String date, int hour, String CF) throws RemoteException {
		for(CatheringInterface ci : catherings) {
			if(CF.equals(ci.getBusinnessNumber())) {
				ci.receiveMessage("There was an infected person in your business [" + date + ":" + hour + "h]" );
			}
		}
		
	}

}
