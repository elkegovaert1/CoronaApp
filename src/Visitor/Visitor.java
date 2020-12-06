package Visitor;

import MixingProxy.Capsule;
import MixingProxy.MixingProxyInterface;
import Registrar.RegistrarInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.PublicKey;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import Doctor.DoctorInterface;

public class Visitor extends UnicastRemoteObject implements VisitorInterface {
    private static final int MAX_VISITS_ALLOWED = 3; // moet 48 zijn
    private String name;
    private String userNumber;
    private RegistrarInterface registrar;
    private MixingProxyInterface mixingProxy;
    private DoctorInterface doctor;
    private int visits;
    private LocalDate lastUpdateTokens;
    private Stack<byte[]> tokens; // if token is used or not
    private List<String> log;
    private PublicKey pk; //To check if QR-code is signed

    public Visitor(String username, String userNumber, DoctorInterface doctor, 
    		RegistrarInterface registrar, MixingProxyInterface mixingProxy) throws RemoteException {
        this.name = username;
        this.userNumber = userNumber;
        this.doctor = doctor;
        this.registrar = registrar;
        this.mixingProxy = mixingProxy;
        this.pk = mixingProxy.getPublicKey();
        this.log = new ArrayList<>();
        this.tokens = new Stack<>();
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
            byte[] token = tokens.pop();
            
            Capsule capsule = new Capsule(LocalTime.now().getHour(), token, information[2].getBytes());

            // naar de mixing proxy sturen (unieke code + vandaag + token)
            boolean accepted = mixingProxy.addCapsule(capsule, (VisitorInterface) this);

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
        List<byte[]> newTokens = registrar.getTokens(userNumber);
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

	@Override
	public void receiveTokens(List<byte[]> tokens) throws RemoteException {
		this.tokens.clear();
		this.tokens.addAll(tokens);		
	}
	//Deze methode wordt gebruikt om een gesigned token up te daten
	@Override
	public void setToken(byte[] oldToken, byte[] newToken) throws RemoteException {
		for(byte[] token : tokens) {
			if(token.equals(oldToken)) {
				token = newToken;
				break;
			}
		}
		
	}

	@Override
	public List<String> getLogsFromTwoDays() throws RemoteException {
		List<String> ret = new ArrayList<>();
		
	}

}
