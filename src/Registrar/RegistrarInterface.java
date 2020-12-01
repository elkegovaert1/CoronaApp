package Registrar;

import Cathering.CatheringInterface;
import Visitor.VisitorInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RegistrarInterface extends Remote {

    boolean newVisitor(VisitorInterface vi) throws RemoteException;
    boolean newCathering(CatheringInterface ci) throws RemoteException;


    void disconnectVisitor(VisitorInterface vi) throws RemoteException;
    void disconnectCathering(CatheringInterface ci) throws RemoteException;

    List<String> getTokens(String number) throws RemoteException;

    boolean checkUserInformation(String number) throws RemoteException;
    boolean checkCatheringInformation(String businessNumber) throws RemoteException;

    String generateDailyQRCode(String businnessNumber) throws RemoteException;

    VisitorInterface getVisitor(String number) throws RemoteException;

}
