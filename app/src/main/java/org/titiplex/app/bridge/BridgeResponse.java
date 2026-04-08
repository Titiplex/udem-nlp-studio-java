package org.titiplex.app.bridge;

public record BridgeResponse<T>(boolean success, String message, T data) {

    public static <T> BridgeResponse<T> ok(T data) {
        return new BridgeResponse<>(true, null, data);
    }

    public static <T> BridgeResponse<T> ok(String message, T data) {
        return new BridgeResponse<>(true, message, data);
    }

    public static <T> BridgeResponse<T> error(String message) {
        return new BridgeResponse<>(false, message, null);
    }
}