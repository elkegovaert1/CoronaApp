package Visitor;

import MixingProxy.MixingProxyInterface;
import Registrar.RegistrarInterface;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class VisitorScreen extends Application {
    public RegistrarInterface registrar;
    public MixingProxyInterface mixingProxy;
    public Visitor visitor;

    public static void main(String[] args) throws RemoteException {
        launch();
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Visitor UI");
        primaryStage.setScene(makeInitScene(primaryStage));
        primaryStage.show();
    }

    public Scene makeInitScene(Stage makeProfile) {
        GridPane rootPane = new GridPane();
        rootPane.setPadding(new Insets(20));
        rootPane.setVgap(10);
        rootPane.setHgap(10);
        rootPane.setAlignment(Pos.CENTER);

        TextField nameField = new TextField();
        TextField numberField = new TextField();

        Label nameLabel = new Label("Name");
        Label numberLabel = new Label("Number");
        Label errorLabel = new Label();

        Button submitVisitorInfoButton = new Button("Done");

        submitVisitorInfoButton.setOnAction(Event -> {
            try {

                Registry registryRegistrar = LocateRegistry.getRegistry("localhost", 1099);
                registrar =  (RegistrarInterface) registryRegistrar.lookup("Registrar");
                Registry registryMixingProxy = LocateRegistry.getRegistry("localhost", 1100);
                mixingProxy = (MixingProxyInterface) registryMixingProxy.lookup("MixingProxy");

                System.out.println("[System] Visitor App is running");

                String username = nameField.getText();
                String userNumber = numberField.getText();
                if (nameField.getText().isEmpty() || numberField.getText().isEmpty()) {
                    errorLabel.setText("Please fill in all fields.");

                } else if (!registrar.checkUserInformation(userNumber)) {
                    errorLabel.setText("Number already in use");

                } else {
                    visitor = new Visitor(username, userNumber, registrar, mixingProxy);

                    boolean isConnected = registrar.newVisitor(visitor);
                    if (!isConnected) {
                        errorLabel.setText("Could not connect.");
                    } else {
                        System.out.println("Visitor connected!");

                        /* Change the scene of the makeProfile */
                        makeProfile.close();
                        makeProfile.setScene(makeHomeUI(makeProfile, visitor, registrar));
                        makeProfile.setTitle(visitor.getName());
                        makeProfile.show();
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
        rootPane.add(submitVisitorInfoButton, 0, 3, 2, 1);
        rootPane.add(errorLabel, 0, 4);

        return new Scene(rootPane, 400, 400);
    }

    public Scene makeHomeUI(Stage home, VisitorInterface vi, RegistrarInterface ri) throws RemoteException{
        GridPane rootPane = new GridPane();
        rootPane.setPadding(new Insets(20));
        rootPane.setAlignment(Pos.CENTER);
        rootPane.setHgap(10);
        rootPane.setVgap(10);

        Button visitCathering = new Button("Visit");

        visitCathering.setOnAction(Event -> {
            home.close();
            home.setScene(makeVisitCatheringUI(home, visitor, registrar));
            home.setTitle("Visit cathering");
            home.show();

        });

        rootPane.add(visitCathering, 0, 0);

        return new Scene(rootPane, 600, 400);

    }

    private Scene makeVisitCatheringUI(Stage visit, Visitor visitor, RegistrarInterface registrar) {
        GridPane rootPane = new GridPane();
        rootPane.setPadding(new Insets(20));
        rootPane.setAlignment(Pos.CENTER);
        rootPane.setHgap(10);
        rootPane.setVgap(10);

        TextField qrcode = new TextField();
        Button visitCathering = new Button("Insert QR-code");
        Label error = new Label();

        visitCathering.setOnAction(Event -> {
            boolean visited = false;
            try {
                visited = visitor.visitCathering(qrcode.getText());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            if (!visited) {
                error.setText("Visit not allowed");
            } else {
                visit.close();
                visit.setScene(makeShowVisitAllowedUI(visit, visitor, registrar));
                visit.setTitle("Visit allowed");
                visit.show();
            }

        });

        rootPane.add(qrcode, 0, 0);
        rootPane.add(visitCathering, 1, 0);
        rootPane.add(error, 0, 1);

        return new Scene(rootPane, 600, 400);
    }

    private Scene makeShowVisitAllowedUI(Stage visitAllowed, Visitor visitor, RegistrarInterface registrar) {
        GridPane rootPane = new GridPane();
        rootPane.setPadding(new Insets(20));
        rootPane.setAlignment(Pos.CENTER);
        rootPane.setHgap(10);
        rootPane.setVgap(10);

        Label allowed = new Label ("visit allowed!");
        Button leaveCathering = new Button("Leave cathering facility");

        leaveCathering.setOnAction(Event -> {
            visitAllowed.close();
            try {
                visitAllowed.setScene(makeHomeUI(visitAllowed, visitor, registrar));
                visitAllowed.setTitle(visitor.getName());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            visitAllowed.show();
        });

        rootPane.add(allowed, 0, 0);
        rootPane.add(leaveCathering,0,1);

        return new Scene(rootPane, 600, 400);
    }

    @Override
    public void stop() throws RemoteException {
        if (registrar != null && visitor != null) {
            visitor.disconnected();
        }
        System.exit(0);
    }
}
