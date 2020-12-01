package Cathering;

import Registrar.RegistrarInterface;
import Visitor.Visitor;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class CatheringScreen extends Application {
    public RegistrarInterface registrar;
    public Cathering cathering;

    public static void main(String[] args) throws RemoteException {
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

        Label nameLabel = new Label("Name");
        Label numberLabel = new Label("Businness Number");
        Label errorLabel = new Label();

        Button submitCatheringInfoButton = new Button("Done");

        submitCatheringInfoButton.setOnAction(Event -> {
            try {

                Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
                registrar =  (RegistrarInterface) myRegistry.lookup("Registrar");

                System.out.println("[System] Cathering App is running");

                String cathname = nameField.getText();
                String businessNumber = numberField.getText();
                if (nameField.getText().isEmpty() || numberField.getText().isEmpty()) {
                    errorLabel.setText("Please fill in all fields.");

                } else if (!registrar.checkCatheringInformation(businessNumber)) {
                    errorLabel.setText("Businness number already in use");

                } else {
                    cathering = new Cathering(cathname, businessNumber, registrar);

                    boolean isConnected = registrar.newCathering(cathering);
                    if (!isConnected) {
                        errorLabel.setText("Could not connect.");
                    } else {
                        System.out.println("Cathering connected!");

                        /* Change the scene of the primaryStage */
                        primaryStage.close();
                        primaryStage.setScene(makeChatUI(cathering, registrar));
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
        rootPane.add(submitCatheringInfoButton, 0, 3, 2, 1);
        rootPane.add(errorLabel, 0, 4);

        return new Scene(rootPane, 400, 400);
    }

    public Scene makeChatUI(CatheringInterface ci, RegistrarInterface ri) throws RemoteException{
        GridPane rootPane = new GridPane();
        rootPane.setPadding(new Insets(20));
        rootPane.setAlignment(Pos.CENTER);
        rootPane.setHgap(10);
        rootPane.setVgap(10);

        cathering.setDailyQRCode();


        Label QRCode = new Label(cathering.getDailyQRCode());

        rootPane.add(QRCode, 0, 0);

        return new Scene(rootPane, 600, 400);

    }

    @Override
    public void stop() throws RemoteException {
        if (registrar != null && cathering != null) {
            cathering.disconnected();
        }
        System.exit(0);
    }

}
