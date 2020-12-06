package MatchingService;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import MixingProxy.Capsule;

public interface MatchingInterface extends Remote {

	void receiveCapsules(List<Capsule> capsules) throws RemoteException;
	
	void receivePosVisitor(List<String> logs) throws RemoteException;
}
