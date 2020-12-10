package Cathering;

import java.rmi.Remote;
import java.rmi.RemoteException;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public interface CatheringInterface extends Remote {

    String getCatheringName() throws RemoteException;
    String getBusinnessNumber() throws RemoteException;
    void disconnected() throws RemoteException;
    void generateDailyQRCode() throws RemoteException;
    StringProperty getDailyQRCode() throws RemoteException;
    String getLocation() throws RemoteException;
    void receivePseudonym(byte[] nym) throws RemoteException;
    void receiveMessage(String s) throws RemoteException;
}
