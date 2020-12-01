package Visitor;

import Registrar.RegistrarInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.util.List;

public class Visitor extends UnicastRemoteObject implements VisitorInterface {
    private String name;
    private String userNumber;
    private RegistrarInterface registrar;
    private List<String> tokens; // if token is used or not
    private List<String> log;

    public Visitor(String username, String userNumber, RegistrarInterface registrar) throws RemoteException {
        this.name = username;
        this.userNumber = userNumber;
        this.registrar = registrar;
        this.tokens = registrar.getTokens(userNumber);
    }

    @Override
    public boolean visitCathering(String QRCode) {
        String [] information = QRCode.split(";");
        log.add(QRCode + ";" + LocalDate.now());
        return true;
    }

    @Override
    public void refresh() throws RemoteException {
        this.tokens = registrar.getTokens(userNumber);
    }

    @Override
    public void disconnected() throws RemoteException {
        registrar.disconnectVisitor(this);
    }

    @Override
    public String getName() throws RemoteException{
        return name;
    }

    @Override
    public String getNumber() throws RemoteException {
        return userNumber;
    }

}
