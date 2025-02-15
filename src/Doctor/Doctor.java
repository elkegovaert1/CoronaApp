package Doctor;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;

public class Doctor implements DoctorInterface {
	
	private PrivateKey sk;
	private PublicKey pk;
	
	public Doctor() {
		try {
			KeyPair keypair = generateRSAKkeyPair();
			sk = keypair.getPrivate();
	        pk = keypair.getPublic();
		} catch (Exception e) {
			e.printStackTrace();
		}   
	}

	public static KeyPair generateRSAKkeyPair() throws Exception{
        SecureRandom secureRandom = new SecureRandom();
 
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
 
        keyPairGenerator.initialize(512, secureRandom);
 
        return keyPairGenerator.generateKeyPair();
	}

	public byte[] signCode(byte[] input) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA"); 
        signature.initSign(sk); 
        signature.update(input); 
        return signature.sign(); 
	}

	public PublicKey getPublicKey() {
		return pk;
	}
	
}
