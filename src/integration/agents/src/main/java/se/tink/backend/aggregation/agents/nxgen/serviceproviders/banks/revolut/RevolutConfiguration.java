package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

public class RevolutConfiguration implements ClientConfiguration {

    @JsonProperty @Secret private String appAuthorization;

    public String getAppAuthorization() {
        return appAuthorization;
    }
}
