package Doctor;
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

public class DoctorScreen extends Application {
	public RegistrarInterface registrar;
    public Doctor doctor;

    public static void main(String[] args) throws RemoteException {
    	//System.setSecurityManager(new SecurityManager());
        launch();
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Cathering UI");
        primaryStage.setScene(makeInitScene(primaryStage));
        primaryStage.show();
    }

    public Scene makeInitScene(Stage primaryStage) {
        GridPane rootPane = new GridPane();
        rootPane.setPadding(new Insets(20));
        rootPane.setVgap(10);
        rootPane.setHgap(10);
        rootPane.setAlignment(Pos.CENTER);

        TextField nameField = new TextField();
        TextField numberField = new TextField();
        TextField locationField = new TextField();

        Label nameLabel = new Label("Name");
        Label numberLabel = new Label("Businness Number");
        Label locationLabel = new Label("Location");
        Label errorLabel = new Label();

        Button submitCatheringInfoButton = new Button("Done");

        submitCatheringInfoButton.setOnAction(Event -> {
            try {
            	Registry createRegistry = LocateRegistry.createRegistry(1102);
            	
            	//System.setProperty("java.rmi.server.hostname","127.0.0.1");
                Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
                registrar =  (RegistrarInterface) myRegistry.lookup("Registrar");

                System.out.println("[System] Doctor App is running");

                String cathname = nameField.getText();
                String businessNumber = numberField.getText();
                String location = locationField.getText();
                if (nameField.getText().isEmpty() || numberField.getText().isEmpty()) {
                    errorLabel.setText("Please fill in all fields.");

                } else if (!registrar.checkCatheringInformation(businessNumber)) {
                    errorLabel.setText("Businness number already in use");

                } else {
                    cathering = new Cathering(cathname, businessNumber, location, registrar);

                    boolean isConnected = registrar.newCathering((CatheringInterface) cathering);
                    if (!isConnected) {
                        errorLabel.setText("Could not connect.");
                    } else {
                        System.out.println("Cathering connected!");

                        /* Change the scene of the primaryStage */
                        primaryStage.close();
                        primaryStage.setScene(makeChatUI(registrar));
                        primaryStage.setTitle(cathering.getCatheringName());
                        primaryStage.show();
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

        });

        rootPane.add(nameField, 0, 0);
        rootPane.add(nameLabel, 1, 0);
        rootPane.add(numberField, 0,1);
        rootPane.add(numberLabel, 1, 1);
        rootPane.add(locationField, 0, 2);
        rootPane.add(locationLabel, 1, 2);
        rootPane.add(submitCatheringInfoButton, 0, 3, 2, 1);
        rootPane.add(errorLabel, 0, 4);

        return new Scene(rootPane, 400, 400);
    }