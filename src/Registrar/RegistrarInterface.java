package Registrar;


import Visitor.VisitorInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;
import java.time.LocalDate;
import java.util.List;

import Cathering.CatheringInterface;



public interface RegistrarInterface extends Remote {

    boolean newVisitor(VisitorInterface vi) throws RemoteException;
    boolean newCathering(CatheringInterface ci) throws RemoteException;


    void disconnectVisitor(VisitorInterface vi) throws RemoteException;
    void disconnectCathering(CatheringInterface catheringInterface) throws RemoteException;


    boolean checkUserInformation(String number) throws RemoteException;
    boolean checkCatheringInformation(String businessNumber) throws RemoteException;

    byte[] generateDailyPseudonym(String businnessNumber, String location) throws RemoteException;

    //function used for inspector
    byte[] getPseudonym(String CF, String date) throws RemoteException;
    
    LocalDate getDate() throws RemoteException;

    boolean checkToken(byte[] visitorToken, byte[] signature, PublicKey key) throws RemoteException;
    
    void flush() throws RemoteException;
    
    void warnVisitor(byte[] visitorToken, String date, String phone) throws RemoteException;
    void warnCathering(String date, int hour, String CF) throws RemoteException;

}
