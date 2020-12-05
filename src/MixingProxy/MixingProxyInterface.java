package MixingProxy;

import MatchingService.MatchingInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MixingProxyInterface extends Remote {

    boolean addCapsule(Capsule capsule) throws RemoteException;

    byte[] controlCapsule(Capsule newCapsule) throws RemoteException;

    void flush(MatchingInterface matchingServer) throws RemoteException;
}
