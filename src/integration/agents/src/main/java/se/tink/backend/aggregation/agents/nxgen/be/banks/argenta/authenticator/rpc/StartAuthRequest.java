package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.ArgentaConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.entity.Device;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class StartAuthRequest {
    Device device;
    String cardNumber;

    public StartAuthRequest(String cardNumber, boolean registered) {
        this.cardNumber = cardNumber;
        this.device =
                new Device(
                        ArgentaConstants.Device.VENDOR,
                        registered,
                        ArgentaConstants.Device.NAME,
                        ArgentaConstants.Device.MODEL,
                        ArgentaConstants.Device.OS_VERSION,
                        ArgentaConstants.Device.OS);
    }
}
