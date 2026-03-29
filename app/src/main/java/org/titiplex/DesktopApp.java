package org.titiplex;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.titiplex.ui.BridgeInstaller;

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

        // RuleService ruleService = context.getBean(RuleService.class);

        WebView webview = new WebView();
        WebEngine engine = webview.getEngine();

        BridgeInstaller.install(engine);

        URL url = getClass().getResource("/webapp/index.html");
        if (url == null) {
            throw new IllegalStateException("Frontend not found, please build and copy into desktop resources.");
        }

        engine.load(url.toExternalForm());

        Scene scene = new Scene(webview, 1400, 800);
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
