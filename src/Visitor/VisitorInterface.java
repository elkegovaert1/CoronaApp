package Visitor;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface VisitorInterface extends Remote {

    void refresh() throws RemoteException;

    String getName() throws RemoteException;
    String getNumber() throws RemoteException;
    void disconnected() throws RemoteException;

    boolean visitCathering(String QRCode) throws RemoteException;

    void updateTokens() throws RemoteException;
    void updateCapsules() throws RemoteException;
}
