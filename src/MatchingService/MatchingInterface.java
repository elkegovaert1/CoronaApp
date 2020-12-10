package MatchingService;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;
import java.util.List;

import MixingProxy.Capsule;

public interface MatchingInterface extends Remote {

	void receiveCapsule(Capsule capsule) throws RemoteException;
	
	void receivePosVisitor(String QRlogs, byte[] signature, PublicKey key) throws RemoteException;
}
