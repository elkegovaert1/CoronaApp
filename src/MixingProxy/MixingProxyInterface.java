package MixingProxy;

import MatchingService.MatchingInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MixingProxyInterface extends Remote {

    void addCapsule(String capsule) throws RemoteException;

    void flush(MatchingInterface matchingServer) throws RemoteException;
}
