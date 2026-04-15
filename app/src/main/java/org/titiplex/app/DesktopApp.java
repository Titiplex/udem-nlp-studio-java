package org.titiplex.app;

import javafx.application.Application;
import javafx.concurrent.Worker;
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
import org.titiplex.app.service.FileDialogService;
import org.titiplex.app.ui.BridgeInstaller;
import org.titiplex.app.ui.FrontendRuntimeAssets;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DesktopApp extends Application {

    private static final Logger LOGGER = Logger.getLogger(DesktopApp.class.getName());

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
        engine.setJavaScriptEnabled(true);

        installDiagnostics(engine, stage);

        AppBridge bridge = context.getBean(AppBridge.class);
        FileDialogService fileDialogService = context.getBean(FileDialogService.class);
        fileDialogService.setOwner(stage);

        BridgeInstaller.install(engine, bridge);

        URL frontendUrl = FrontendRuntimeAssets.prepareIndexUrl();
        LOGGER.info(() -> "Loading frontend from " + frontendUrl);
        engine.load(frontendUrl.toExternalForm());

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

    private void installDiagnostics(WebEngine engine, Stage stage) {
        engine.setOnError(event ->
                LOGGER.severe("[webview] " + event.getMessage()));

        engine.setOnAlert(event ->
                LOGGER.info("[frontend alert] " + event.getData()));

        engine.locationProperty().addListener((obs, oldValue, newValue) ->
                LOGGER.info(() -> "[webview] location = " + newValue));

        engine.titleProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && !newValue.isBlank()) {
                stage.setTitle(newValue);
            }
        });

        engine.getLoadWorker().exceptionProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                LOGGER.log(Level.SEVERE, "Frontend exception", newValue);
            }
        });

        engine.getLoadWorker().stateProperty().addListener((obs, oldValue, newValue) -> {
            LOGGER.info(() -> "[webview] load state = " + newValue);
            if (newValue == Worker.State.FAILED) {
                showFrontendError(engine.getLoadWorker().getException());
            }
        });
    }

    private void showFrontendError(Throwable error) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Frontend error");
        alert.setHeaderText("Le frontend n’a pas pu être chargé");
        alert.setContentText(error == null
                ? "Aucune exception Java disponible. Vérifie les logs WebView."
                : error.toString());
        alert.showAndWait();
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