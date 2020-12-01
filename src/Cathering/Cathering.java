package Cathering;

import Registrar.RegistrarInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Cathering extends UnicastRemoteObject implements CatheringInterface {
    private String catheringName;
    private String businnessNumber;
    private RegistrarInterface registrar;
    private String dailyQRCode;

    public Cathering(String catheringName, String businnessNumber, RegistrarInterface registrar) throws RemoteException {
        this.catheringName = catheringName;
        this.registrar = registrar;
        this.businnessNumber = businnessNumber;
    }

    @Override
    public void setDailyQRCode() throws RemoteException {
        this.dailyQRCode = registrar.generateDailyQRCode(businnessNumber);
    }

    @Override
    public void disconnected () throws RemoteException {
        registrar.disconnectCathering(this);
    }

    @Override
    public String getCatheringName() throws RemoteException {
        return catheringName;
    }

    @Override
    public String getBusinnessNumber() throws RemoteException {
        return businnessNumber;
    }

    @Override
    public String getDailyQRCode() {
        return dailyQRCode;
    }


}
