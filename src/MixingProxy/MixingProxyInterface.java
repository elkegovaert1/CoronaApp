package MixingProxy;

import MatchingService.MatchingInterface;
import Visitor.VisitorInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;

public interface MixingProxyInterface extends Remote {

    boolean addCapsule(Capsule capsule, VisitorInterface vi) throws RemoteException;

    byte[] controlCapsule(Capsule newCapsule) throws RemoteException;

    void flush(MatchingInterface matchingServer) throws RemoteException;
    
    PublicKey getPublicKey() throws RemoteException;
}
