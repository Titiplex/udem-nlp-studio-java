package org.titiplex.bridge;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.titiplex.dto.BridgeResponse;

public class AppBridge {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String ping() {
        return write(BridgeResponse.ok("pong"));
    }

    public String getAppInfo() {
        return write(BridgeResponse.ok(new AppInfo("My Desktop App", "1.0.0-SNAPSHOT")));
    }

    private String write(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "{\"success\":false,\"message\":\"Serialization error\"}";
        }
    }

    public record AppInfo(String name, String version) {
    }
}
