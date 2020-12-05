package MixingProxy;

import MatchingService.MatchingInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MixingProxyInterface extends Remote {

    boolean addCapsule(String capsule) throws RemoteException;

    boolean controlCapsule(String newCapsule) throws RemoteException;

    void flush(MatchingInterface matchingServer) throws RemoteException;
}
