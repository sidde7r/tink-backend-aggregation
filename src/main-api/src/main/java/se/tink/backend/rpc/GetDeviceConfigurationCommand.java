package se.tink.backend.rpc;

import com.google.common.base.Preconditions;
import java.util.UUID;

public class GetDeviceConfigurationCommand {
    private UUID deviceId;
    private String desiredMarket;

    public GetDeviceConfigurationCommand(String deviceId, String desiredMarket) {
        validate(deviceId, desiredMarket);
        this.deviceId = UUID.fromString(deviceId);
        this.desiredMarket = desiredMarket;
    }

    private void validate(String deviceId, String market) {
        Preconditions.checkNotNull(deviceId);
        Preconditions.checkNotNull(market);
        // check that deviceId is a valid UUID
        // the UUID.fromString validates if the string argument is a valid UUID or not
        // depending on how we use this, make sure that the validation takes place somewhere.r
    }

    public UUID getDeviceId() {
        return deviceId;
    }

    public String getDesiredMarket() {
        return desiredMarket;
    }
}
