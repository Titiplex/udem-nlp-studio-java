package org.titiplex.ui;

import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;
import org.titiplex.bridge.AppBridge;

public class BridgeInstaller {
    public static void install(WebEngine engine) {
        AppBridge bridge = new AppBridge();

        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("appBridge", bridge);
            }
        });
    }
}
