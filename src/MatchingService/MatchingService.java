package MatchingService;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import MixingProxy.Capsule;
import Registrar.RegistrarInterface;

public class MatchingService extends UnicastRemoteObject implements MatchingInterface {

    /**
	 * 
	 */
	private static final long serialVersionUID = 8962611190032735929L;
	public static ObservableList<Capsule> capsules;
    private RegistrarInterface registrar;

    public MatchingService(RegistrarInterface ri) throws RemoteException {
        capsules = FXCollections.observableArrayList();
        registrar = ri;
    }
    public void receiveCapsule(Capsule capsule) throws RemoteException {
    	Platform.runLater(() -> {
    		capsules.add(capsule);
    	});    	
    }
	@Override
	public void receivePosVisitor(String QRlogs, byte[] signature, PublicKey key) throws RemoteException {
		try {
			if(Verify_Digital_Signature(QRlogs.getBytes(), signature, key)) {
				String[] logs = QRlogs.split(";;;");
				for(String log: logs) {
					String[] arr = log.split(";;");
					
					String QRCathering = arr[0];
					String[] information = QRCathering.split(";");
					String R = information[0];
					String CF = information[1];
					String HRnym = information[2];
					
					String date = arr[1];
					int hour = Integer.parseInt(arr[2]);
					String phoneNumberOfInfection = arr[3]; //de geïnfecteerde mag geen message ontvangen (denk ik?)
					warnVisitors(HRnym, date, hour, phoneNumberOfInfection);
					warnCathering(date, hour, CF);
				}
			}
		} catch (Exception e) {
			System.out.println("Signature van log komen niet overeen");
			e.printStackTrace();
		}
		
		
	}
	public void warnVisitors(String HRnym, String date, int hour, String phone) {
		byte[] code = DatatypeConverter.parseHexBinary(HRnym);
		for(Capsule c : capsules) {
			if(Arrays.equals(c.getCatheringCode(), code) && c.getTime() == hour) { //Date zit in catheringCode
				try {
					registrar.warnVisitor(c.getVisitorToken(), date, phone);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
		//Ik stel voor om 24 tokens per dag te maken, dan hebben we 24 intervallen op een dag 
		//en hoeven we enkel het afgeronde uur mee te geven in de capsules en logs
	}
	public void warnCathering(String date, int hour, String CF) throws RemoteException {
				try {
					registrar.warnCathering(date, hour,  CF);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
		
	}
	public static boolean Verify_Digital_Signature(byte[] input, byte[] signatureToVerify, PublicKey key)throws Exception 
    { 
        Signature signature = Signature.getInstance("SHA256withRSA"); 
        signature.initVerify(key); 
        signature.update(input); 
        return signature.verify(signatureToVerify); 
    }
    
}
