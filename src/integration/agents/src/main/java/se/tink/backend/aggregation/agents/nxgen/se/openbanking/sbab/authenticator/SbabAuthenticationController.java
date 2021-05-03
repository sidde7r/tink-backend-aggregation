package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.contexts.SystemUpdater;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SbabAuthenticationController implements Authenticator {
    private final BankIdAuthenticationController bankIdAuthenticationController;
    private final SystemUpdater systemUpdater;
    private final CredentialsRequest request;

    public SbabAuthenticationController(
            CredentialsRequest request,
            SystemUpdater systemUpdater,
            BankIdAuthenticationController bankIdAuthenticationController) {
        this.systemUpdater = systemUpdater;
        this.request = request;
        this.bankIdAuthenticationController = bankIdAuthenticationController;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        // Due to using AutoAuthenticationController previously, some credentials will have the type
        // changed to PASSWORD, which makes the app refresh them without prompting the user for
        // authentication. This is considered a background refresh, and can only be done 4 times per
        // day.
        if (credentials.getType() == CredentialsTypes.PASSWORD) {
            // Reset credential type to MOBILE_BANKID and expire session
            credentials.setType(CredentialsTypes.MOBILE_BANKID);
            systemUpdater.updateCredentialsExcludingSensitiveInformation(credentials, false);
            throw SessionError.SESSION_EXPIRED.exception();
        }

        if (request.getUserAvailability().isUserAvailableForInteraction()) {
            bankIdAuthenticationController.authenticate(credentials);
        } else {
            bankIdAuthenticationController.autoAuthenticate();
        }
    }
}
