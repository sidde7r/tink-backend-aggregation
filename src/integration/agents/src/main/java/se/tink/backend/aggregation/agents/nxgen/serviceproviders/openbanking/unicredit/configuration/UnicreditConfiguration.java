package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class UnicreditConfiguration implements ClientConfiguration {

    @Secret private String baseUrl;

    @Secret private String psuIdType;

    public String getBaseUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(baseUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Base URL"));

        return baseUrl;
    }

    public String getPsuIdType() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(psuIdType),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "PSU Id Type"));

        return psuIdType;
    }
}
