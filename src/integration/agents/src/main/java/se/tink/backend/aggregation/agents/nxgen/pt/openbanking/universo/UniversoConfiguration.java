package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.universo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.pt.openbanking.universo.UniversoConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class UniversoConfiguration implements ClientConfiguration {

    @Secret private String apiKey;

    @JsonIgnore
    public String getApiKey() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(apiKey),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "API key"));

        return apiKey;
    }
}
