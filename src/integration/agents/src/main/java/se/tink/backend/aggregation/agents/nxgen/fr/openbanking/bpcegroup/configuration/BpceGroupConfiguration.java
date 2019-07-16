package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.BpceGroupConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class BpceGroupConfiguration implements ClientConfiguration {

    private String clientId;
    private String clientSecret;

    public String getClientId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client ID"));

        return clientId;
    }

    public String getClientSecret() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSecret),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client Secret"));

        return clientSecret;
    }
}
