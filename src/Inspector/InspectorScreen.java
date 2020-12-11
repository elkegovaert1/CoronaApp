package Inspector;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import Cathering.Cathering;
import Cathering.CatheringInterface;
import Registrar.RegistrarInterface;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class InspectorScreen extends Application {
	public RegistrarInterface registrar;
	public Inspector inspector;

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
            Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
            registrar =  (RegistrarInterface) myRegistry.lookup("Registrar");
            inspector = new Inspector(registrar);
        }catch(Exception e) {
        	e.printStackTrace();
        }
        TextField qrField = new TextField();
        
        
        Label qrLabel = new Label("QR-code");
        Label errorLabel = new Label();

        Button submitCatheringInfoButton = new Button("Done");

        submitCatheringInfoButton.setOnAction(Event -> {
            
        		try {
					if(inspector.inspectCathering(qrField.getText())) {
						errorLabel.setText("QRCode is correct");
					}else {
						errorLabel.setText("Invalid QRCode");
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}

        });
        rootPane.add(qrField, 0, 1);
        rootPane.add(qrLabel, 0, 0);
        rootPane.add(submitCatheringInfoButton, 1, 1);
        rootPane.add(errorLabel, 1, 0);

        return new Scene(rootPane, 400, 400);
    }
}
