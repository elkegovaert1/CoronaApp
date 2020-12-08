package MatchingService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

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
    public void receiveCapsules(List<Capsule> capsules) throws RemoteException {
    	this.capsules.addAll(capsules);
    }
	@Override
	public void receivePosVisitor(List<String> logs) throws RemoteException {
		for(String log: logs) {
			String[] arr = log.split(";");
			String R = arr[0];
			String CF = arr[1];
			String HRnym = arr[2];
			String datetime = arr[3];
			warnVisitors(HRnym, datetime);
			warnCathering(HRnym, datetime, CF, R);
		}
		
	}
	public void warnVisitors(String HRnym, String datetime) {
		//TODO 
		//Ik stel voor om 24 tokens per dag te maken, dan hebben we 24 intervallen op een dag 
		//en hoeven we enkel het afgeronde uur mee te geven in de capsules en logs
	}
	public void warnCathering(String HRnym, String datetime, String CF, String R) {
		for(Capsule c : capsules) {
			if(HRnym.equals(c.getCatheringCode().toString())) {
				try {
					registrar.informCathering(datetime, CF);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
    
}
