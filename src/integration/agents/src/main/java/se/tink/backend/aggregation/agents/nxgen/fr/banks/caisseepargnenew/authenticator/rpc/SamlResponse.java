package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.CaisseEpargneConstants.ResponseValues;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.entities.Saml2Post;

@Getter
public class SamlResponse {

    @JsonProperty("saml2_post")
    private Saml2Post saml2Post;

    @JsonProperty("status")
    private String status;

    @JsonProperty("unlockingDate")
    private String unlockingDate;

    @JsonIgnore
    public void throwIfFailedAuthentication() throws AuthorizationException, LoginException {
        if (isAuthenticationBlockedResponse()) {
            throw AuthorizationError.ACCOUNT_BLOCKED.exception();
        } else if (isAuthenticationFailedResponse()) {
            throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception();
        }
        if (!isAuthenticationSuccessResponse()) {
            throw LoginError.INVALIDATED_CREDENTIALS.exception();
        }
    }

    @JsonIgnore
    private boolean isAuthenticationSuccessResponse() {
        return Optional.ofNullable(status)
                .filter(ResponseValues.AUTHENTICATION_SUCCESS::equals)
                .isPresent();
    }

    @JsonIgnore
    private boolean isAuthenticationBlockedResponse() {
        return Optional.ofNullable(status)
                .filter(ResponseValues.AUTHENTICATION_LOCKED::equals)
                .isPresent();
    }

    @JsonIgnore
    private boolean isAuthenticationFailedResponse() {
        return Optional.ofNullable(status)
                .filter(ResponseValues.AUTHENTICATION_FAILED::equals)
                .isPresent();
    }
}
