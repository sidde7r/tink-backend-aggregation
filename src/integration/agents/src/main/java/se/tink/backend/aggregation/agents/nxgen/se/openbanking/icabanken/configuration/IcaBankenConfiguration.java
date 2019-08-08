package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class IcaBankenConfiguration implements ClientConfiguration {

    @JsonProperty private String clientId;
    @JsonProperty private String redirectUri;
    @JsonProperty private String certificateId;

    public String getClientId() {
        return clientId;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public String getCertificateId() {
        return certificateId;
    }
}
