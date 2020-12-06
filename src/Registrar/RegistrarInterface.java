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

    byte[] generateDailyPseudonym(String businnessNumber, String location) throws RemoteException;

    VisitorInterface getVisitor(String number) throws RemoteException;
    
    void informCathering(String HRnym, String datetime, String CF, String R) throws RemoteException;
    
    //function used for inspector
    byte[] getPseudonym(CatheringInterface ci, String date) throws RemoteException;

}
