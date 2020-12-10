package Visitor;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface VisitorInterface extends Remote {

    //void refresh() throws RemoteException;

    String getName() throws RemoteException;
    String getNumber() throws RemoteException;
    void disconnected() throws RemoteException;
    
    void receiveTokens(List<byte[]> tokens) throws RemoteException;

    boolean didntExitCathering() throws RemoteException;
    boolean visitCathering(String QRCode) throws RemoteException;

    //void updateTokens() throws RemoteException;
    
    void getLogsFromTwoDays() throws RemoteException;
    
    void receiveMessage(String s) throws RemoteException;

}
