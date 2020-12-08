package Inspector;

import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import javax.xml.bind.DatatypeConverter;

import Cathering.CatheringInterface;
import Registrar.RegistrarInterface;

public class Inspector implements InspectorInterface {
	
	private RegistrarInterface registrar;
	
	public Inspector(RegistrarInterface ri) {
		registrar = ri;
	}
	
	public boolean inspectCathering(String QRCode) throws RemoteException {
		System.out.println("input: " + QRCode);
		String[] arr = QRCode.split(";");
		String R = arr[0];
		String CF = arr[1];
		LocalDate lDate = registrar.getDate();
		String date = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).format(lDate);
		System.out.println("Date: " + date);
		byte[] nym = null;
		try {
			nym = registrar.getPseudonym(CF, date);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String dailyQRCode = null;
		try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(DatatypeConverter.parseHexBinary(R));//Salt represent R
            byte[] hashcode = md.digest(nym);//Represents H(Ri, nym), has to be used for signature
            dailyQRCode = R + ";" + CF + ";" + 
            			DatatypeConverter.printHexBinary(hashcode);
    	}catch(NoSuchAlgorithmException e) {
    		e.printStackTrace();
    	}
		System.out.println("output: " + dailyQRCode);
		if(QRCode.equals(dailyQRCode)) {
			return true;
		}else {
			return false;
		}
	}
	
}
