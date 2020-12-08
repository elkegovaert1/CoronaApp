package Doctor;

import java.rmi.RemoteException;
import java.util.List;

import MatchingService.MatchingInterface;
import Visitor.VisitorInterface;

public class Doctor implements DoctorInterface{
	
	private MatchingInterface matchingService;
	
	public Doctor(MatchingInterface mi) {
		matchingService = mi;
	}

	@Override
	public void consult(VisitorInterface vi) throws RemoteException {
		// TODO Auto-generated method stub
		
	}
	
	public static void resultOfConsult(boolean resTest, VisitorInterface vi) {
		if(resTest) { //true is positive
			//List<String> logs = vi.getLogsFromTwoWeeks(); //TODO
			//matchingService.receiveLogs(logs) //TODO
		}else { //negative
			//do nothing
		}
	}
	
}
