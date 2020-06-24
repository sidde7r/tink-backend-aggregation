package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.KbcConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.configuration.BerlinGroupConfiguration;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;

@JsonObject
public class KbcConfiguration implements BerlinGroupConfiguration {

    @JsonProperty @Secret private String clientId;

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public String getClientSecret() {
        throw new NotImplementedException("clientSecret not used anymore");
    }

    @Override
    public String getBaseUrl() {
        return KbcConstants.Urls.BASE_URL;
    }

    @Override
    public String getPsuIpAddress() {
        return "0.0.0.0";
    }
}
