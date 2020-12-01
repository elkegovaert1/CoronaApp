package MatchingService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class MatchingService extends UnicastRemoteObject implements MatchingInterface {

    public static ObservableList<String> capsules;

    public MatchingService() throws RemoteException {
        capsules = FXCollections.observableArrayList();
    }
}
