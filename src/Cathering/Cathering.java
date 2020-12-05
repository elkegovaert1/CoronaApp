package Cathering;

import Registrar.RegistrarInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Cathering extends UnicastRemoteObject implements CatheringInterface {
    private String catheringName;
    private String businnessNumber;
    private RegistrarInterface registrar;
    private String dailyQRCode;
    private byte[] nym;
    private String location;

    public Cathering(String catheringName, String businnessNumber, RegistrarInterface registrar) throws RemoteException {
        this.catheringName = catheringName;
        this.registrar = registrar;
        this.businnessNumber = businnessNumber;
    }
    
    @Override
    public void generateDailyQRCode() throws RemoteException {
    	try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] R = getSalt();
            md.update(R);//Salt represent R
            byte[] hash = md.digest(nym);//Represents H(Ri, nym) has to be used for signature
            dailyQRCode = R.toString() + ";" + businnessNumber + ";" + hash;
    	}catch(NoSuchAlgorithmException e) {
    		e.printStackTrace();
    	}    	
    }

    @Override
    public void disconnected () throws RemoteException {
        registrar.disconnectCathering(this);
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
    public String getDailyQRCode() {
        return dailyQRCode;
    }

	@Override
	public String getLocation() throws RemoteException {
		return location;
	}

	@Override
	public void setDailyQRCode() throws RemoteException {
		// TODO Auto-generated method stub
		
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


}
