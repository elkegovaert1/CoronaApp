package MixingProxy;

import MatchingService.MatchingInterface;
import Visitor.VisitorInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;

public interface MixingProxyInterface extends Remote {

    boolean addCapsule(Capsule capsule, VisitorInterface vi) throws RemoteException;

    byte[] controlCapsule(Capsule newCapsule, VisitorInterface vi) throws RemoteException;

    void flush() throws RemoteException;
    
    PublicKey getPublicKey() throws RemoteException;
}
