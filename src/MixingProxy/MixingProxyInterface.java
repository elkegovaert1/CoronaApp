package MixingProxy;

import MatchingService.MatchingInterface;
import Visitor.VisitorInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;

public interface MixingProxyInterface extends Remote {

    byte[] addCapsule(Capsule capsule, VisitorInterface vi) throws RemoteException;

    byte[] controlCapsule(Capsule newCapsule, VisitorInterface vi) throws RemoteException;

    void flush() throws RemoteException;
    
    int getHour() throws RemoteException;
    
    PublicKey getPublicKey() throws RemoteException;
}
