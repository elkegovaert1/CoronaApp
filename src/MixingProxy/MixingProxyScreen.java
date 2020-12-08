package MixingProxy;

import MatchingService.MatchingInterface;
import Registrar.RegistrarInterface;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class MixingProxyScreen extends Application {
    public MatchingInterface matchingServer;
    public MixingProxy mixingProxy;

    public static void main(String[] args) throws RemoteException {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.setTitle("Mixing Proxy");
        primaryStage.setScene(makeUI(primaryStage));
        primaryStage.show();

    }

    public Scene makeUI(Stage primaryStage) throws IOException {
        GridPane rootPane = new GridPane();
        rootPane.setAlignment(Pos.CENTER);
        rootPane.setPadding(new Insets(20));
        rootPane.setVgap(10);
        rootPane.setHgap(10);

        Registry registryCreate = LocateRegistry.createRegistry(1100);
        // find matching server
        try {
            Registry myRegistryGet = LocateRegistry.getRegistry("localhost", 1101);
            matchingServer = (MatchingInterface) myRegistryGet.lookup("MatchingService");
            mixingProxy = new MixingProxy(matchingServer);
            registryCreate.rebind("MixingProxy", mixingProxy);
            System.out.println("[System] Mixing proxy is ready.");
        } catch (Exception e) {
            e.printStackTrace();
        }


        Label queueLabel = new Label("Queue of capsules");
        ListView<Capsule> queueView = new ListView<>();
        queueView.setItems(mixingProxy.capsules);

        Button flushCapsules = new Button("Flush");

        flushCapsules.setOnAction(Event -> {
            try {
                mixingProxy.flush();
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        });

        rootPane.add(queueLabel, 0, 0);
        rootPane.add(queueView, 0, 1);
        rootPane.add(flushCapsules, 0,2);

        primaryStage.show();

        return new Scene(rootPane, 400, 300);

    }

    @Override
    public void stop() throws RemoteException {
        System.exit(0);
    }

}
