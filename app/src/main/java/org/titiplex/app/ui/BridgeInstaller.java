package org.titiplex.app.ui;

import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;
import org.titiplex.app.bridge.AppBridge;

public final class BridgeInstaller {

    private BridgeInstaller() {
    }

    public static void install(WebEngine engine, AppBridge bridge) {
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("appBridge", bridge);
            }
        });
    }
}