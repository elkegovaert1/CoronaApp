package Visitor;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface VisitorInterface extends Remote {

    void refresh() throws RemoteException;

    String getName() throws RemoteException;
    String getNumber() throws RemoteException;
    void disconnected() throws RemoteException;
    
    void receiveTokens(List<byte[]> tokens) throws RemoteException;

    boolean visitCathering(String QRCode) throws RemoteException;

    void updateTokens() throws RemoteException;
    
    void setToken(byte[] oldToken, byte[] newToken) throws RemoteException;
    
    List<String> getLogsFromTwoDays() throws RemoteException;

}
