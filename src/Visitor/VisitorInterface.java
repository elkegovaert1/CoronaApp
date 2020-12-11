package Visitor;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface VisitorInterface extends Remote {

    String getName() throws RemoteException;
    String getNumber() throws RemoteException;
    void disconnected() throws RemoteException;
    
    void receiveTokens(List<byte[]> tokens) throws RemoteException;

    boolean didNotExitCathering() throws RemoteException;
    boolean visitCathering(String QRCode, VisitorScreen vs) throws IOException;

    void getLogsFromTwoDays() throws RemoteException;
    
    void receiveMessage(String s) throws RemoteException;

    void createImage(byte[] accepted) throws IOException;

}
