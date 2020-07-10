package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class DeutscheBankConfiguration implements ClientConfiguration {
    @JsonProperty @Secret private String baseUrl;
    @JsonProperty @Secret private String psuIdType;

    public String getBaseUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(baseUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Base URL"));

        return baseUrl;
    }

    public String getPsuIdType() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(psuIdType),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "PSU ID Type"));

        return psuIdType;
    }
}
