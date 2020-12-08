package Doctor;

import java.rmi.Remote;
import java.rmi.RemoteException;

import Visitor.VisitorInterface;

public interface DoctorInterface extends Remote{
	
	void consult(VisitorInterface vi) throws RemoteException;

}
