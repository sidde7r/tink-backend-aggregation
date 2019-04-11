package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.MonzoConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class DemoFakeBankConfiguration implements ClientConfiguration {

    private String baseUrl;

    public String getBaseUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(baseUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Base URL"));

        return baseUrl;
    }
}
