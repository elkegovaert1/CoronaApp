package MixingProxy;

import MatchingService.MatchingInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface MixingProxyInterface extends Remote {

    void addCapsule(String capsule) throws RemoteException;
    void addCapsules(List<String> capsules) throws RemoteException;

    void flush(MatchingInterface matchingServer) throws RemoteException;
}
