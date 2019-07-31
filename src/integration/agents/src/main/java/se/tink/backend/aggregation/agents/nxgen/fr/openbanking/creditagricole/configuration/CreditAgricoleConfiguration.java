package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.CreditAgricoleConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class CreditAgricoleConfiguration implements ClientConfiguration {

    private String token;
    private String redirectUrl;

    public String getToken() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(token),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Token"));
        return token;
    }

    public String getRedirectUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Redirect url"));
        return redirectUrl;
    }
}
