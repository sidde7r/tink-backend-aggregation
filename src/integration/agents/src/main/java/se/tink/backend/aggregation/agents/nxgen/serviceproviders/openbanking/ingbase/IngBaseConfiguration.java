package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase;

import java.net.InetAddress;
import java.net.UnknownHostException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants.PaymentRequest;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class IngBaseConfiguration implements ClientConfiguration {

    public String getPsuIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return PaymentRequest.DEFAULT_IP;
        }
    }
}
