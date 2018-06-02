package se.tink.backend.rpc;

import com.google.common.base.Preconditions;
import java.util.UUID;

public class GetProvidersByDeviceCommand {
    private UUID deviceId;
    private String market;

    public GetProvidersByDeviceCommand(String deviceId, String market) {
        validate(deviceId, market);
        this.deviceId = UUID.fromString(deviceId);
        this.market = market;
    }

    private void validate(String deviceId, String market) {
        Preconditions.checkNotNull(deviceId);
        Preconditions.checkNotNull(market);
    }

    public UUID getDeviceId() {
        return deviceId;
    }

    public String getMarket() {
        return market;
    }
}
