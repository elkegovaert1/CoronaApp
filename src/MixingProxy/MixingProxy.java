package MixingProxy;

/*
A mix proxy shuffles incoming messages
(i.e. capsules - see further) and flushes them at regular time
intervals to the matching service. Further, a central health authority
(not included in figure 1) mediates interactions between the general
practitioner and the matching service

Every time the app sends a capsule to the mixing server (i.e. two
times an hour in our prototype), the mixing server adds the time to
this entry. This way, users cannot lie about the time he has visited
that location. The user is prevented from delaying the transmission
of an entry to the mixing server, as the catering facility demands a
proof (i.e. the visual code) that the capsule was sent correctly to the
mixing server

The mixing server holds the capsules of all users during some time
interval. Shortly after the time interval has finished, the capsules
are flushed to the matching service in a random order. The data is
removed from the database of the matching service after a
predefined time interval, which can be imposed by the government
(with a minimum of one day to prevent multiple spending of the
same user token)
 */

import MatchingService.MatchingInterface;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class MixingProxy extends UnicastRemoteObject implements MixingProxyInterface {

    public static ObservableList<String> capsules;

    public MixingProxy () throws RemoteException {
        capsules = FXCollections.observableArrayList();
    }

    public void addCapsule(String capsule) throws RemoteException {
        Platform.runLater(() -> capsules.add(capsule));
    }

    @Override
    public void addCapsules(List<String> capsules) throws RemoteException {
        Platform.runLater(() -> capsules.addAll(capsules));
    }

    public void flush(MatchingInterface msi) throws RemoteException{

    }

}
