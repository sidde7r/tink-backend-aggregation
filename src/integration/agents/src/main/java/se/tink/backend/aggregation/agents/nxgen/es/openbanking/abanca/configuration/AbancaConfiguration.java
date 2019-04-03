package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class AbancaConfiguration implements ClientConfiguration {

    private String authKey;

    public String getAuthKey() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(authKey),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Auth Key"));

        return authKey;
    }
}
