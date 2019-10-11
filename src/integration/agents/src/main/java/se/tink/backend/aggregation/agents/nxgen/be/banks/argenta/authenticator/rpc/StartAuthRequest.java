package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.ArgentaConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.entity.Device;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class StartAuthRequest {
    private Device device;
    private String cardNumber;

    public StartAuthRequest(
            final String cardNumber, final boolean registered, final String aggregator) {
        this.cardNumber = cardNumber;
        this.device =
                new Device(
                        aggregator,
                        registered,
                        aggregator,
                        ArgentaConstants.Device.MODEL,
                        ArgentaConstants.Device.OS_VERSION,
                        ArgentaConstants.Device.OS);
    }
}
