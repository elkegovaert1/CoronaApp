package Registrar;

import Cathering.CatheringInterface;
import Visitor.VisitorInterface;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


/*

The registrar has three major tasks.
First, it enrolls new catering facilities and provides them with a tool
to generate QR codes on a daily basis. Second, it enrolls new users
and provides them with tokens that can be used when visiting a
catering facility. Third, it reveals contact information of possibly
infected people. The matching service keeps information about
visits and supports contact tracing. Note that uniquely identifying
user and catering facility data are not revealed to the matching
service

Note that the registrar keeps the mapping between the phone number
and the tokens that were issued

 */

public class Registrar extends UnicastRemoteObject implements RegistrarInterface {

    private List<VisitorInterface> visitors;
    private List <CatheringInterface> catherings;

    public static ObservableList<String> visitorNameNumber;
    public static ObservableList<String> catheringNameNumber;

    private Map<String, List<String>> userTokens;
    private Map<String, LocalDate> dateGeneratedTokens; // wanneer tokens laatste keer gegenereerd

    public Registrar() throws RemoteException {
        visitors = new ArrayList<>();
        catherings = new ArrayList<>();
        visitorNameNumber = FXCollections.observableArrayList();
        catheringNameNumber = FXCollections.observableArrayList();
    }

    @Override
    public boolean newVisitor(VisitorInterface vi) throws RemoteException {

        Platform.runLater(() -> {
            try {
                visitors.add(vi);
                visitorNameNumber.add(vi.getName() + "[" +vi.getNumber() + "]");
                System.out.println("New user: " + vi.getName());

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
        return true;
    }

    @Override
    public boolean newCathering(CatheringInterface ci) throws RemoteException {

        Platform.runLater(() -> {
            try {
                catherings.add(ci);
                catheringNameNumber.add(ci.getCatheringName() + "[" +ci.getBusinnessNumber() + "]");
                System.out.println("New cathering facility: " + ci.getCatheringName());

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
        return true;
    }

    @Override
    public boolean checkCatheringInformation(String businessNumber) throws RemoteException {
        for (CatheringInterface ci: catherings) {
            if (ci.getBusinnessNumber().equals(businessNumber)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean checkUserInformation(String number) throws RemoteException {
        for (VisitorInterface visitor: visitors) {
            if (number.equals(visitor.getNumber())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<String> getTokens(String number) throws RemoteException {
        LocalDate today = LocalDate.now();

        LocalDate lastUpdate = dateGeneratedTokens.get(number);

        if (today.isBefore(lastUpdate) || today.isEqual(lastUpdate)) {
            return null;
        } else {
            dateGeneratedTokens.replace(number, today);
            List<String> newTokens = new ArrayList<>();
            newTokens.add("1");
            newTokens.add("2");
            newTokens.add("3");
            return newTokens;
        }
    }

    @Override
    public VisitorInterface getVisitor(String number) throws RemoteException{
        for(VisitorInterface visitorInterface : visitors) {
            if(visitorInterface.getNumber().equals(number)) {
                return visitorInterface;
            }
        }
        return null;
    }

    @Override
    public void disconnectVisitor(VisitorInterface vi) throws RemoteException {
        try {
            String info = vi.getName() + "[" +vi.getNumber() + "]";
            visitorNameNumber.removeAll(info);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        visitors.remove(vi);

    }

    @Override
    public void disconnectCathering(CatheringInterface ci) throws RemoteException {
        try {
            catheringNameNumber.removeAll(ci.getCatheringName() + "[" +ci.getBusinnessNumber() + "]");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        catherings.remove(ci);
    }

    @Override
    public String generateDailyQRCode(String businnessNumber) throws RemoteException {
        // R random number
        // CF cathering facility
        // H(R,nym) a day-specific pseudonym 𝑛𝑦𝑚𝐶𝐹,𝑑𝑎𝑦𝑖 for the catering facility as
        //      follows (with H being a cryptographic hash function):
        return "R;CF;H(R,nym)";
    }

}
