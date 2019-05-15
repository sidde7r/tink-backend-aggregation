package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class HandelsbankenBaseConfiguration implements ClientConfiguration {

    @JsonProperty private String clientId;
    @JsonProperty private String tppTransactionId;
    @JsonProperty private String tppRequestId;
    @JsonProperty private String psuIpAddress;

    public String getClientId() {
        return clientId;
    }

    public String getTppTransactionId() {
        return tppTransactionId;
    }

    public String getTppRequestId() {
        return tppRequestId;
    }

    public String getPsuIpAddress() {
        return psuIpAddress;
    }
}
