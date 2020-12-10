package Doctor;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

import Cathering.Cathering;
import Cathering.CatheringInterface;
import Inspector.Inspector;
import MatchingService.MatchingInterface;
import Registrar.RegistrarInterface;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class DoctorScreen extends Application {
	public MatchingInterface matchingService;
	public Doctor doctor;

    public static void main(String[] args) throws RemoteException {
        launch();
    }
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Inspector");
        primaryStage.setScene(makeInitScene(primaryStage));
        primaryStage.show();
    }

    public Scene makeInitScene(Stage primaryStage) {
        GridPane rootPane = new GridPane();
        rootPane.setPadding(new Insets(20));
        rootPane.setVgap(10);
        rootPane.setHgap(10);
        rootPane.setAlignment(Pos.CENTER);
        try {
        	//System.setProperty("java.rmi.server.hostname","127.0.0.1");
            Registry myRegistry = LocateRegistry.getRegistry("localhost", 1101);
            matchingService =  (MatchingInterface) myRegistry.lookup("MatchingService");
            doctor = new Doctor(matchingService);
        }catch(Exception e) {
        	e.printStackTrace();
        }
        TextField qrField = new TextField();      
        
        Label qrLabel = new Label("QR-code");

        Button submitCatheringInfoButton = new Button("Done");

        submitCatheringInfoButton.setOnAction(Event -> {
            
        		primaryStage.setScene(makeTestScene(primaryStage, qrField.getText()));

        });
        rootPane.add(qrField, 0, 1);
        rootPane.add(qrLabel, 0, 0);
        rootPane.add(submitCatheringInfoButton, 1, 1);

        return new Scene(rootPane, 400, 400);
    }
    public Scene makeTestScene(Stage primaryStage, String QRCode) {
    	GridPane rootPane = new GridPane();
        rootPane.setPadding(new Insets(20));
        rootPane.setVgap(10);
        rootPane.setHgap(10);
        rootPane.setAlignment(Pos.CENTER);
        
        ToggleGroup group = new ToggleGroup();

        RadioButton rb1 = new RadioButton("Positive");
        rb1.setToggleGroup(group);
        rb1.setSelected(true);

        RadioButton rb2 = new RadioButton("Negative");
        rb2.setToggleGroup(group);
        
        Button submitTestButton = new Button("Done");

        submitTestButton.setOnAction(Event -> {
            	if(rb1.isSelected()) {
            		try {
            			byte[] signature;
						try {
							signature = doctor.signCode(QRCode.getBytes());
							matchingService.receivePosVisitor(QRCode, signature, doctor.getPublicKey());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}catch(Exception e) {
						e.printStackTrace();
					}
            	}
        		primaryStage.setScene(makeInitScene(primaryStage));

        });
        rootPane.add(rb1, 0, 0);
        rootPane.add(rb2, 0, 1);
        rootPane.add(submitTestButton, 1, 0);
        return new Scene(rootPane, 400, 400);
    }
}