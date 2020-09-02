package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.RaiffeisenConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;

@JsonObject
public class RaiffeisenConfiguration implements ClientConfiguration {

    @Secret @ClientIdConfiguration private String clientId;

    public String getClientId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client ID"));

        return clientId;
    }
}
