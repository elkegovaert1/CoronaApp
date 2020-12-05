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

Upon receiving a capsule, the mixing server first checks (a) the
validity of the user token, and then verifies that (b) it is a token for
that particular day and (c) it has not been spent before

This capsule contains the current time interval, a valid user token 𝑇𝑥 ,𝑑𝑎𝑦𝑖
𝑢ser and the 3rd value in the QR code (i.e., 𝐻(𝑅𝑖, 𝑛𝑦𝑚𝐶𝐹,𝑑𝑎𝑦𝑖) ).
 */

import MatchingService.MatchingInterface;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.util.List;

public class MixingProxy extends UnicastRemoteObject implements MixingProxyInterface {

    public static ObservableList<String> capsules;

    public MixingProxy () throws RemoteException {
        capsules = FXCollections.observableArrayList();
    }

    @Override
    public boolean addCapsule(String newCapsule) throws RemoteException {
        boolean accepted = controlCapsule(newCapsule);

        // voeg capsules toe
        if (accepted) {
            Platform.runLater(() -> capsules.add(newCapsule + LocalDate.now().toString())); // mixing proxy voegt nogmaals data toe als check
            return true;
        } else {
            return false;
        }

    }

    @Override
    public boolean controlCapsule(String newCapsule) throws RemoteException {
        // controle legite capsule
        String [] information = newCapsule.split(";");
        String code = information[0];
        String date = information[1];
        String token = information[2];

        // TODO: control the validity of the user token

        // controle datum
        String now = LocalDate.now().toString();
        if (!now.equals(date)) {
            return false;
        }

        // als het niet al bevat
        for (String c: capsules) {
            String [] capsuleSplit = newCapsule.split(";");
            if (capsuleSplit[2].equals(token)) {
                return false;
            }
        }


        return true;
    }

    @Override
    public void flush(MatchingInterface msi) throws RemoteException{
        // send to matching server

        // remove data
        capsules.clear();
    }

}
