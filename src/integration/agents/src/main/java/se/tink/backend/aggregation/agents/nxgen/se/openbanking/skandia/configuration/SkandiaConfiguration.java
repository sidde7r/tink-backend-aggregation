package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class SkandiaConfiguration implements ClientConfiguration {

    private String consentId;

    public String getConsentId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(consentId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Consent ID"));

        return consentId;
    }
}
