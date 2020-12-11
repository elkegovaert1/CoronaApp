package Registrar;

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
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import com.sun.glass.ui.View;

public class RegistrarScreen extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.setTitle("Registrar");
        primaryStage.setScene(makeUI(primaryStage));
        primaryStage.show();
    }

    public Scene makeUI(Stage primaryStage) throws IOException {
        GridPane rootPane = new GridPane();
        rootPane.setAlignment(Pos.CENTER);
        rootPane.setPadding(new Insets(20));
        rootPane.setVgap(10);
        rootPane.setHgap(10);

        Registry registry = LocateRegistry.createRegistry(1099);
        Registrar registrar = new Registrar();
        registry.rebind("Registrar", registrar);
        System.out.println("[System] Registrar is ready.");

        Label visitorsLabel = new Label("Visitors");
        ListView<String> visitorView = new ListView<>();
        visitorView.setItems(registrar.visitorNameNumber);

        Label catheringLabel = new Label("Catherings");
        ListView<String> catheringView = new ListView<>();
        catheringView.setItems(registrar.catheringNameNumber);
        
        Button newDay = new Button("New Day");
        newDay.setOnAction(Event -> {
            try {
            	registrar.newDay();
            }catch(Exception e) {
            	e.printStackTrace();
            }
            });
        rootPane.add(visitorsLabel, 0, 0);
        rootPane.add(visitorView, 0, 1);
        rootPane.add(catheringLabel, 1, 0);
        rootPane.add(catheringView, 1, 1);
        rootPane.add(newDay, 1, 2);

        primaryStage.show();

        return new Scene(rootPane, 400, 300);

    }

    @Override
    public void stop() throws RemoteException {
        System.exit(0);
    }
}
