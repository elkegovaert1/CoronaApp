package MatchingService;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import MixingProxy.Capsule;
import Registrar.RegistrarInterface;


public class MatchingScreen extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.setTitle("Matching Service");
        primaryStage.setScene(makeUI(primaryStage));
        primaryStage.show();
    }

    public Scene makeUI(Stage primaryStage) throws IOException {
        GridPane rootPane = new GridPane();
        rootPane.setAlignment(Pos.CENTER);
        rootPane.setPadding(new Insets(20));
        rootPane.setVgap(10);
        rootPane.setHgap(10);

        Registry createRegistry = LocateRegistry.createRegistry(1101);
        Registry registrarRegistry = LocateRegistry.getRegistry("localhost", 1099);
        RegistrarInterface registrar;
        MatchingService matchingService = null;
		try {
			registrar = (RegistrarInterface) registrarRegistry.lookup("Registrar");
			matchingService = new MatchingService(registrar);
			createRegistry.rebind("MatchingService", matchingService);
	        System.out.println("[System] Matching Service is ready.");
		} catch (NotBoundException e) {
			e.printStackTrace();
		}       
        
        Label matchingServerState = new Label("State of Matching Server");
        ListView<Capsule> mss = new ListView<>();
        mss.setItems(matchingService.capsules);

        rootPane.add(matchingServerState, 0, 0);
        rootPane.add(mss, 0, 1);

        primaryStage.show();

        return new Scene(rootPane, 400, 300);

    }

    @Override
    public void stop() throws RemoteException {
        System.exit(0);
    }
}
