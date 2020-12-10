package Cathering;


import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableStringValue;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.xml.bind.DatatypeConverter;

import Registrar.RegistrarInterface;

public class Cathering extends UnicastRemoteObject implements CatheringInterface {
    /**
	 * 
	 */
	private static final long serialVersionUID = -8574034096812967296L;
	private String catheringName;
    private String businnessNumber;
    private RegistrarInterface registrar;
    private byte[] nym;
    private String location;
    private StringProperty dailyQRCode;

    public Cathering(String catheringName, String businnessNumber, String location, RegistrarInterface registrar) throws RemoteException {
        this.catheringName = catheringName;
        this.registrar = registrar;
        this.location = location;
        this.businnessNumber = businnessNumber;
        dailyQRCode = new SimpleStringProperty();
    }
    
    @Override
    public void generateDailyQRCode() throws RemoteException {
    	try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] R = getSalt();
            md.update(R);//Salt represent R
            byte[] hash = md.digest(nym);//Represents H(Ri, nym), has to be used for signature
            this.dailyQRCode.setValue(DatatypeConverter.printHexBinary(R) + ";" + businnessNumber + ";" +
            		DatatypeConverter.printHexBinary(hash));
            CatheringScreen.setQrcode(dailyQRCode.getValue());
            System.out.println("QR: " + dailyQRCode);
    	}catch(NoSuchAlgorithmException e) {
    		e.printStackTrace();
    	}    	
    }

    @Override
    public void disconnected () throws RemoteException {
        registrar.disconnectCathering((CatheringInterface) this);
    }

    @Override
    public String getCatheringName() throws RemoteException {
        return catheringName;
    }

    @Override
    public String getBusinnessNumber() throws RemoteException {
        return businnessNumber;
    }

    @Override
    public StringProperty getDailyQRCode() {
    	System.out.println("QR-code: " + dailyQRCode);
        return dailyQRCode;
    }

	@Override
	public String getLocation() throws RemoteException {
		return location;
	}


	@Override
	public void receivePseudonym(byte[] nym) throws RemoteException {
		this.nym = nym;
		generateDailyQRCode();
	}
	private static byte[] getSalt() throws NoSuchAlgorithmException {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt); //R
        return salt;
    }

	@Override
	public void receiveMessage(String s) throws RemoteException {
		System.out.println(s);
		
	}


}
