package Registrar;


import Visitor.VisitorInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.List;

import Cathering.CatheringInterface;



public interface RegistrarInterface extends Remote {

    boolean newVisitor(VisitorInterface vi) throws RemoteException;
    boolean newCathering(CatheringInterface ci) throws RemoteException;


    void disconnectVisitor(VisitorInterface vi) throws RemoteException;
    void disconnectCathering(CatheringInterface catheringInterface) throws RemoteException;

    List<String> getTokens(String number) throws RemoteException;

    boolean checkUserInformation(String number) throws RemoteException;
    boolean checkCatheringInformation(String businessNumber) throws RemoteException;

    byte[] generateDailyPseudonym(String businnessNumber, String location) throws RemoteException;

    VisitorInterface getVisitor(String number) throws RemoteException;
    
    void informCathering(String datetime, String CF) throws RemoteException;
    
    //function used for inspector
    byte[] getPseudonym(String CF, String date) throws RemoteException;
    
    LocalDate getDate() throws RemoteException;

}
