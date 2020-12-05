package Visitor;

import MixingProxy.MixingProxyInterface;
import Registrar.RegistrarInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Visitor extends UnicastRemoteObject implements VisitorInterface {
    private static final int MAX_VISITS_ALLOWED = 3; // moet 48 zijn
    private String name;
    private String userNumber;
    private RegistrarInterface registrar;
    private MixingProxyInterface mixingProxy;
    private int visits;
    private LocalDate lastUpdateTokens;
    private List<String> tokens; // if token is used or not
    private List<String> log;

    public Visitor(String username, String userNumber, RegistrarInterface registrar, MixingProxyInterface mixingProxy) throws RemoteException {
        this.name = username;
        this.userNumber = userNumber;
        this.registrar = registrar;
        this.mixingProxy = mixingProxy;
        this.tokens = registrar.getTokens(userNumber);
        this.log = new ArrayList<>();
        visits = 0;
        lastUpdateTokens = LocalDate.now();
    }

    @Override
    public boolean visitCathering(String QRCode) throws RemoteException {
        // als aan max aantal visits || mogen tokens van gisteren niet gebruiken
        LocalDate now = LocalDate.now();
        if (visits >= MAX_VISITS_ALLOWED || lastUpdateTokens.isBefore(LocalDate.now())) {
            return false;
        } else {
            // QRCode ontmantelen
            String [] information = QRCode.split(";");

            // toevoegen aan capsules die moeten verzonden worden
            String token = tokens.get(0);

            // naar de mixing proxy sturen (unieke code + vandaag + token)
            boolean accepted = mixingProxy.addCapsule(QRCode + ";" + LocalDate.now().toString() + ";" + token);

            // opslaan in log
            if (accepted) {
                log.add(QRCode + ";" + LocalDate.now().toString());
                tokens.remove(token);

                visits++;
                return true;
            } else {
                return false;
            }

        }
    }

    // opgeroepen als zogezegd elke keer dat scherm geopend wordt
    @Override
    public void refresh() throws RemoteException {
        updateTokens();
    }

    // kijken of er nieuwe tokens opgehaald moeten worden
    @Override
    public void updateTokens() throws RemoteException {
        List<String> newTokens = registrar.getTokens(userNumber);
        if (!newTokens.isEmpty()) { // krijgt geen nieuwe tokens als die er al gehad heeft die dag
            this.tokens = newTokens;
        }
        lastUpdateTokens = LocalDate.now();
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
