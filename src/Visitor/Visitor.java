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
    private static final int MAX_DAYS_SENDING_CAPSULES = 1;
    private String name;
    private String userNumber;
    private RegistrarInterface registrar;
    private MixingProxyInterface mixingProxy;
    private int visits;
    private LocalDate lastUpdateTokens;
    private LocalDate lastUpdateCapsules;
    private List<String> tokens; // if token is used or not
    private List<String> log;
    private List<String> capsulesToBeSend;

    public Visitor(String username, String userNumber, RegistrarInterface registrar, MixingProxyInterface mixingProxy) throws RemoteException {
        this.name = username;
        this.userNumber = userNumber;
        this.registrar = registrar;
        this.mixingProxy = mixingProxy;
        this.tokens = registrar.getTokens(userNumber);
        this.log = new ArrayList<>();
        this.capsulesToBeSend = new ArrayList<>();
        visits = 0;
        lastUpdateTokens = LocalDate.now();
        lastUpdateCapsules = LocalDate.now().minusDays(MAX_DAYS_SENDING_CAPSULES);
    }

    @Override
    public boolean visitCathering(String QRCode) {
        // als aan max aantal visits || mogen tokens van gisteren niet gebruiken
        LocalDate now = LocalDate.now();
        if (visits >= MAX_VISITS_ALLOWED || lastUpdateTokens.isBefore(LocalDate.now())) {
            return false;
        } else {
            visits++;
            // QRCode ontmantelen
            String [] information = QRCode.split(";");

            // opslaan in log
            log.add(QRCode + ";" + LocalDate.now().toString());

            // toevoegen aan capsules die moeten verzonden worden
            String token = tokens.get(0);
            tokens.remove(token);
            capsulesToBeSend.add(QRCode + ";" + LocalDate.now().toString() + ";" + token);
            return true;
        }
    }

    // opgeroepen als zogezegd elke keer dat scherm geopend wordt
    @Override
    public void refresh() throws RemoteException {
        updateTokens();
        updateCapsules();
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

    // kijken ofdat capsules moeten doorgestuurd worden
    @Override
    public void updateCapsules() throws RemoteException {
        LocalDate sendDate = lastUpdateCapsules.plusDays(MAX_DAYS_SENDING_CAPSULES);
        if (sendDate.isBefore(lastUpdateCapsules.plusDays(MAX_DAYS_SENDING_CAPSULES))) {
            mixingProxy.addCapsules(capsulesToBeSend);
            capsulesToBeSend.clear();
        }
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
