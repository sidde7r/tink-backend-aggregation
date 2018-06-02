package se.tink.backend.export.model.submodels;

import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.core.UserDevice;
import se.tink.backend.core.UserDeviceStatuses;
import se.tink.backend.export.helper.DefaultSetter;

public class ExportDevice implements DefaultSetter{

    private final String updated;
    private final String status;
    private final String userAgent; // TODO: Expand/deserialize this
    private final String payload;// TODO: Expand/deserialize this

    public ExportDevice(Date updated, UserDeviceStatuses status, String userAgent, Map<UserDevice.PayloadKey, String> payload) {
        this.updated = notNull(updated);
        this.status = notNull(status);
        this.payload = deserializePayload(payload);
        this.userAgent = userAgent;
    }

    public String getUpdated() {
        return updated;
    }

    public String getStatus() {
        return status;
    }

    public String getPayload() {
        return payload;
    }

    public String getUserAgent() {
        return userAgent;
    }

    private String deserializePayload(Map<UserDevice.PayloadKey, String> payload) {
        return payload.entrySet().stream().map(e -> e.getKey().toString() + ": " + e.getValue())
                .collect(Collectors.joining());
    }
}
