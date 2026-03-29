package org.titiplex.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class BridgeResponse<T> {
    private boolean success;
    private T data;
    private String response;

    public static <T> BridgeResponse<T> ok(T data) {
        return new BridgeResponse<>(true, data, null);
    }

    public static <T> BridgeResponse<T> error(String message) {
        return new BridgeResponse<>(false, null, message);
    }
}
