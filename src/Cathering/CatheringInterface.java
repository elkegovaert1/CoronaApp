package Cathering;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CatheringInterface extends Remote {

    String getCatheringName() throws RemoteException;
    String getBusinnessNumber() throws RemoteException;
    void disconnected() throws RemoteException;
    void generateDailyQRCode() throws RemoteException;
    String getDailyQRCode() throws RemoteException;
    String getLocation() throws RemoteException;
    void receivePseudonym(byte[] nym) throws RemoteException;
}
