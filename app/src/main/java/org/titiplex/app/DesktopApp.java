package org.titiplex.app;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.titiplex.app.bridge.AppBridge;
import org.titiplex.app.ui.BridgeInstaller;

import java.net.URL;

public class DesktopApp extends Application {

    private ConfigurableApplicationContext context;

    @Override
    public void init() {
        context = new SpringApplicationBuilder(DesktopApplication.class)
                .web(WebApplicationType.NONE)
                .run();
    }

    @Override
    public void start(Stage stage) {
        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();

        AppBridge bridge = context.getBean(AppBridge.class);
        BridgeInstaller.install(engine, bridge);

        URL url = getClass().getResource("/webapp/index.html");
        if (url == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Frontend introuvable");
            alert.setHeaderText("Le frontend Vue n'a pas été empaqueté");
            alert.setContentText("Lance un build Maven qui génère le frontend puis le copie dans /webapp.");
            alert.showAndWait();
            throw new IllegalStateException("Frontend not found in classpath: /webapp/index.html");
        }

        engine.load(url.toExternalForm());

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double width = Math.min(1400, bounds.getWidth() - 40);
        double height = Math.min(900, bounds.getHeight() - 60);

        Scene scene = new Scene(webView, width, height);
        stage.setTitle("NLP Studio");
        stage.setScene(scene);
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.centerOnScreen();
        stage.show();
    }

    @Override
    public void stop() {
        if (context != null) {
            context.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}