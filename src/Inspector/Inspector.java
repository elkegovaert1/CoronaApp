package Inspector;

import Registrar.RegistrarInterface;

import javax.xml.bind.DatatypeConverter;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class Inspector implements InspectorInterface {

    private final RegistrarInterface registrar;

    public Inspector(RegistrarInterface ri) {
        registrar = ri;
    }

    public boolean inspectCathering(String QRCode) throws RemoteException {
        System.out.println("input: " + QRCode);
        String[] arr = QRCode.split(";");
        String R = arr[0];
        String CF = arr[1];
        LocalDate lDate = registrar.getDate();
        String date = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).format(lDate);
        System.out.println("Date: " + date);
        byte[] nym = null;
        try {
            nym = registrar.getPseudonym(CF, date);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        String dailyQRCode = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(DatatypeConverter.parseHexBinary(R));//Salt represent R
            byte[] hashcode = md.digest(nym);//Represents H(Ri, nym), has to be used for signature
            dailyQRCode = R + ";" + CF + ";" +
                    DatatypeConverter.printHexBinary(hashcode);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            return false;
        }
        System.out.println("output: " + dailyQRCode);
        return QRCode.equals(dailyQRCode);
    }

}
