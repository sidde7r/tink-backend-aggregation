package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.IspConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CheckTimeRequest {

    @JsonProperty("BT")
    private String username;

    @JsonProperty("DeviceID")
    private String deviceId;

    @JsonProperty("DeviceTime")
    private String deviceTime;

    public CheckTimeRequest(
            final String username, final String deviceId, final LocalDateTime deviceTime) {
        this.username = Objects.requireNonNull(username);
        this.deviceId = Objects.requireNonNull(deviceId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(IspConstants.DATE_TIME_FORMAT);
        this.deviceTime =
                Objects.requireNonNull(deviceTime.atZone(ZoneId.of("UTC"))).format(formatter);
    }
}
