package Visitor;

import MixingProxy.Capsule;
import MixingProxy.MixingProxyInterface;
import Registrar.RegistrarInterface;
import javafx.application.Platform;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.PublicKey;
import java.time.LocalDate;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.bind.DatatypeConverter;

import Doctor.DoctorInterface;

public class Visitor extends UnicastRemoteObject implements VisitorInterface {

	private static final long serialVersionUID = -4542222821435686339L;
	private static final int MAX_VISITS_ALLOWED = 24; // moet 48 zijn
    private String name;
    private String userNumber;
    private String QRcode;
    private RegistrarInterface registrar;
    private MixingProxyInterface mixingProxy;
    private DoctorInterface doctor;
    private int visits;
    private LocalDate lastUpdateTokens;
    private Stack<byte[]> tokens; // if token is used or not
    private List<String> log;
    private PublicKey pk; //To check if QR-code is signed

    public Visitor(String username, String userNumber,  
    		RegistrarInterface registrar, MixingProxyInterface mixingProxy) throws RemoteException {
        this.name = username;
        this.userNumber = userNumber;
        this.doctor = doctor;
        this.registrar = registrar;
        this.mixingProxy = mixingProxy;
        this.pk = mixingProxy.getPublicKey();
        this.log = new ArrayList<>();
        this.tokens = new Stack<>();
        /////////////////////////////////////
        visits = 0;						  ///
        lastUpdateTokens = LocalDate.now();//Lijkt mij niet nodig
        /////////////////////////////////////
    }
    public void exitCathering() {
    	QRcode = null;
    }

    @Override
    public boolean didNotExitCathering() throws RemoteException{
    	if(QRcode == null) {
    		return false;
    	}
    	// als aan max aantal visits || mogen tokens van gisteren niet gebruiken
        LocalDate now = registrar.getDate();
        if (visits >= MAX_VISITS_ALLOWED || lastUpdateTokens.isBefore(LocalDate.now())) {
        	System.out.println("we zitten hier fout");
            return false;
        } else {
            // QRCode ontmantelen
            String [] information = QRcode.split(";");
            byte[] HRnym = DatatypeConverter.parseHexBinary(information[2]);
            // toevoegen aan capsules die moeten verzonden worden
            byte[] token = tokens.pop();
            
            Capsule capsule = new Capsule(mixingProxy.getHour(), token, HRnym);

            // naar de mixing proxy sturen (unieke code + vandaag + token)
            boolean accepted = mixingProxy.addCapsule(capsule, (VisitorInterface) this);

            // opslaan in log
            if (accepted) {
            	 Platform.runLater(() -> {
            		 LocalDate lDate;
					try {
						lDate = registrar.getDate();
						int hour = mixingProxy.getHour();
	                     log.add(QRcode + ";;" + DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).format(lDate)
	                    		 + ";;" + hour);
	                     System.out.println("Log: " + log.get(log.size() - 1));
					} catch (RemoteException e) {
						e.printStackTrace();
						System.out.println("Er ging iets mis met logging");
					}            	
                     visits++;
            	 });
            	
                return true;
            } else {
            	System.out.println("Capsule not accepted");
                return false;
            }

        }
    }

    @Override
    public boolean visitCathering(String QRCode) throws RemoteException {
    	this.QRcode = QRCode; //for sending new capsules next hour
        // als aan max aantal visits || mogen tokens van gisteren niet gebruiken
        LocalDate now = registrar.getDate();
        if (visits >= MAX_VISITS_ALLOWED || lastUpdateTokens.isBefore(LocalDate.now())) {
        	System.out.println("we zitten hier fout");
            return false;
        } else {
            // QRCode ontmantelen
            String [] information = QRCode.split(";");
            byte[] HRnym = DatatypeConverter.parseHexBinary(information[2]);
            // toevoegen aan capsules die moeten verzonden worden
            byte[] token = tokens.pop();
            
            Capsule capsule = new Capsule(mixingProxy.getHour(), token, HRnym);

            // naar de mixing proxy sturen (unieke code + vandaag + token)
            boolean accepted = mixingProxy.addCapsule(capsule, (VisitorInterface) this);

            // opslaan in log
            if (accepted) {
            	 Platform.runLater(() -> {
            		 LocalDate lDate;
					try {
						lDate = registrar.getDate();
						int hour = mixingProxy.getHour();
	                    log.add(QRCode + ";;" + DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).format(lDate)
	                    		 + ";;" + hour);
	                     System.out.println("Log: " + log.get(log.size() - 1));
					} catch (RemoteException e) {
						e.printStackTrace();
						System.out.println("Er ging iets mis met logging");
					}            	
                     visits++;
            	 });
            	
                return true;
            } else {
            	System.out.println("Capsule not accepted");
                return false;
            }

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

	@Override
	public void receiveTokens(List<byte[]> tokens) throws RemoteException {
		this.tokens.clear();
		this.tokens.addAll(tokens);
	}

	@Override
	public void getLogsFromTwoDays() throws RemoteException {
		
		//QR-Code: 'Log1';;;'Log2'...
		//Log: R;CF;H(R,nym);;datum;;hour;;phonenumber
			//|QR-Cathering|
		List<String> ret = new ArrayList<>();
		LocalDate lDate = registrar.getDate();
		for(String s : log) {
			String[] arr = s.split(";;");
			String[] datum = arr[1].split("/");
			String maand = datum[1];
			String dag = datum[0];
			String jaar = "20" + datum[2];
			LocalDate logDate = LocalDate.of(Integer.parseInt(jaar), Integer.parseInt(maand), Integer.parseInt(dag));
			if(logDate.isAfter(lDate.minus(7, ChronoUnit.DAYS))) {//alle logs van 7 dagen geleden
				ret.add(arr[0] + ";;" + arr[1] + ";;" + arr[2] + ";;" + userNumber);
				System.out.println(ret.size() - 1);
			}else if(logDate.isBefore(lDate.minus(7, ChronoUnit.DAYS))) {
				System.out.println("Data komt verkeerd uit");
			}
			
		}
		StringBuilder sb = new StringBuilder();
		for(String s : ret) {
			sb.append(s);
			sb.append(";;;");
		}
		String str = sb.toString();
		String string = str.substring(0, str.length() - 3);
		System.out.println("QRLogs: " + string);
	}

	@Override
	public void receiveMessage(String s) throws RemoteException {
		System.out.println(s);
		
	}

}
