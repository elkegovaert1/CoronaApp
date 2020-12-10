package MixingProxy;

/*
A mix proxy shuffles incoming messages
(i.e. capsules - see further) and flushes them at regular time
intervals to the matching service. Further, a central health authority
(not included in figure 1) mediates interactions between the general
practitioner and the matching service

Every time the app sends a capsule to the mixing server (i.e. two
times an hour in our prototype), the mixing server adds the time to
this entry. This way, users cannot lie about the time he has visited
that location. The user is prevented from delaying the transmission
of an entry to the mixing server, as the catering facility demands a
proof (i.e. the visual code) that the capsule was sent correctly to the
mixing server

The mixing server holds the capsules of all users during some time
interval. Shortly after the time interval has finished, the capsules
are flushed to the matching service in a random order. The data is
removed from the database of the matching service after a
predefined time interval, which can be imposed by the government
(with a minimum of one day to prevent multiple spending of the
same user token)

Upon receiving a capsule, the mixing server first checks (a) the
validity of the user token, and then verifies that (b) it is a token for
that particular day and (c) it has not been spent before*/



import MatchingService.MatchingInterface;
import Registrar.RegistrarInterface;
import Visitor.VisitorInterface;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;

public class MixingProxy extends UnicastRemoteObject implements MixingProxyInterface {

    /**
	 * 
	 */
	private static final long serialVersionUID = 6198417477899452462L;
	public static ObservableList<Capsule> capsules;
    private static PrivateKey sk; //used to sign capsules
    private static PublicKey pk; //used by visitor to verify signing
    private MatchingInterface matchingService;
    private RegistrarInterface registrar;
    private int hour;

    public MixingProxy (MatchingInterface mi, RegistrarInterface ri) throws RemoteException {
        capsules = FXCollections.observableArrayList();
        KeyPair keypair;
		try {
			keypair = generateRSAKkeyPair();
			sk = keypair.getPrivate();
	        pk = keypair.getPublic();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}        
        matchingService = mi;
        registrar = ri;
        hour = LocalTime.now().getHour();
    }
    
    @Override
    public boolean addCapsule(Capsule newCapsule, VisitorInterface vi) throws RemoteException {
        byte[] signing = controlCapsule(newCapsule, vi);
        boolean accepted = false;
		try {
			accepted = Verify_Digital_Signature(newCapsule.getCatheringCode(), signing, pk);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        	
        
        // voeg capsules toe
        if (accepted) {
            Platform.runLater(() -> {
            capsules.add(newCapsule);
            System.out.println("New capsule: " + newCapsule.getTime() + ", " + 
            		DatatypeConverter.printHexBinary(newCapsule.getVisitorToken()) + ", " + 
            		DatatypeConverter.printHexBinary(newCapsule.getCatheringCode()));
            });
            return true;
        } else {
            return false;
        }

    }

    @Override
    public byte[] controlCapsule(Capsule newCapsule, VisitorInterface vi) throws RemoteException {
        // controle legite capsule
        byte[] code = newCapsule.getCatheringCode();
        int hourCapsule = newCapsule.getTime();
        byte[] token = newCapsule.getVisitorToken();
        
        boolean isValid = true;
        System.out.println("CatheringCode: " + DatatypeConverter.printHexBinary(code));
        System.out.println("VisitorToken: " + DatatypeConverter.printHexBinary(token));
        byte[] signing;
		try {
			signing = signCode(token);
			System.out.println("VisitorToken[Signed]: " + DatatypeConverter.printHexBinary(signing));
			if(registrar.checkToken(token, signing, pk)) {
	        	newCapsule.setVisitorToken(signing);
	        }else {
	        	System.out.println("visitorToken not accepted");
	        	isValid = false;
	        }  
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			isValid = false;
		}
        
             
       /* the mixing server first checks (a) the
        validity of the user token, and then verifies that (b) it is a token for
        that particular day and (c) it has not been spent before*/
        // a) zit in checkToken bij de registrar
        // b) zit ook in checkToken bij de registrar (na elke dag worden de niet-signed tokens verwijderd)
        // c) zit in signCode, elk token kan maar één keer gesigned worden
        // controle datum

        // als het niet al bevat -> is overbodig denk ik
        /*for (Capsule c: capsules) {
            if (c.getVisitorToken().equals(token)) {
                isValid =  false;
            }
        }*/
        if(isValid) {
        	try {
        		System.out.println("Gesigned: " + DatatypeConverter.printHexBinary(
        				signCode(newCapsule.getCatheringCode())));
				return signCode(newCapsule.getCatheringCode());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
        }else {
        	System.out.println("Original: " + DatatypeConverter.printHexBinary(newCapsule.getCatheringCode()));
        	return newCapsule.getCatheringCode();
        }
    }

    @Override
    public void flush() throws RemoteException{
    	for(Capsule c : capsules) {
    		matchingService.receiveCapsule(c);
    	}        

        // remove data
        capsules.clear();
        registrar.flush();
    }
    public static KeyPair generateRSAKkeyPair() throws Exception{
            SecureRandom secureRandom = new SecureRandom();
     
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
     
            keyPairGenerator.initialize(512, secureRandom);
     
            return keyPairGenerator.generateKeyPair();
    }
    
    public static byte[] signCode(byte[] input) throws Exception{ 
            Signature signature = Signature.getInstance("SHA256withRSA"); 
            signature.initSign(sk); 
            signature.update(input); 
            return signature.sign(); 
    }

	@Override
	public PublicKey getPublicKey() throws RemoteException {
		return pk;
	}
	public static boolean Verify_Digital_Signature(byte[] input, byte[] signatureToVerify, PublicKey key)throws Exception 
    { 
        Signature signature = Signature.getInstance("SHA256withRSA"); 
        signature.initVerify(key); 
        signature.update(input); 
        return signature.verify(signatureToVerify); 
    }

	@Override
	public int getHour() throws RemoteException {
		return hour;
	}
	
	public void incrementHour() {
		this.hour++;
	}
}
