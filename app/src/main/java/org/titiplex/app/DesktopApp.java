package org.titiplex.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.titiplex.backend.BackendApplication;
import org.titiplex.app.bridge.AppBridge;
import org.titiplex.app.ui.BridgeInstaller;

import java.net.URL;

public class DesktopApp extends Application {

    private ConfigurableApplicationContext context;

    @Override
    public void init() {
        context = new SpringApplicationBuilder(BackendApplication.class)
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
            throw new IllegalStateException("Frontend not found, please build frontend and copy dist to /webapp.");
        }

        engine.load(url.toExternalForm());

        Scene scene = new Scene(webView, 1400, 900);
        stage.setTitle("NLP Studio");
        stage.setScene(scene);
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