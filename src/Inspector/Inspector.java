package Inspector;

import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import Cathering.CatheringInterface;
import Registrar.RegistrarInterface;

public class Inspector implements InspectorInterface {
	
	private RegistrarInterface registrar;
	
	public Inspector(RegistrarInterface ri) {
		registrar = ri;
	}
	
	private boolean inspectCathering(CatheringInterface ci, String QRCode) {
		String[] arr = QRCode.split(";");
		String R = arr[0];
		String CF = arr[1];
		String date = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).format(LocalDate.now());
		byte[] nym = null;
		try {
			nym = registrar.getPseudonym(ci, date);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String dailyQRCode = null;
		try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(R.getBytes());//Salt represent R
            byte[] hashcode = md.digest(nym);//Represents H(Ri, nym), has to be used for signature
            dailyQRCode = R + ";" + CF + ";" + hashcode;
    	}catch(NoSuchAlgorithmException e) {
    		e.printStackTrace();
    	}
		if(QRCode.equals(dailyQRCode)) {
			return true;
		}else {
			return false;
		}
	}
	
}
