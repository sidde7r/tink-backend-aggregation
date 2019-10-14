package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.ArgentaConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.entity.Device;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class StartAuthRequest {
    private Device device;
    private String cardNumber;

    public StartAuthRequest(String cardNumber, boolean registered, final String aggregator) {
        this.cardNumber = cardNumber;
        this.device =
                new Device(
                        ArgentaConstants.Device.UNKNOWN,
                        registered,
                        aggregator,
                        ArgentaConstants.Device.UNKNOWN,
                        ArgentaConstants.Device.UNKNOWN,
                        ArgentaConstants.Device.UNKNOWN);
    }
}
