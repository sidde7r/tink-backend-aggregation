package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps;

import java.lang.invoke.MethodHandles;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.SwedbankBalticsConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.SwedbankBalticsAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.libraries.credentials.service.CredentialsRequest;

@RequiredArgsConstructor
public class InitStep implements AuthenticationStep {

    private final SwedbankBalticsAuthenticator authenticator;
    private final SwedbankApiClient apiClient;
    // TODO: wrapper for session Storage
    private final SessionStorage sessionStorage;
    private final CredentialsRequest credentialsRequest;
    private final Provider provider;

    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        final Credentials credentials = request.getCredentials();

        final String userId =
                authenticator.verifyCredentialsNotNullOrEmpty(
                        credentials.getField(Field.Key.USERNAME));

        final String personalId =
                authenticator.verifyCredentialsNotNullOrEmpty(
                        credentials.getField(Key.NATIONAL_ID_NUMBER));

        // TODO: separate function ?
        // This is Special case for Sweden, should we use it here?
        if (!credentialsRequest.getUserAvailability().isUserAvailableForInteraction()) {
            if (credentialsRequest.getType() == CredentialsRequestType.MANUAL_AUTHENTICATION) {
                // note that request type "MANUAL_AUTHENTICATION" is misleading and will, in this
                // case (with
                // User _Not_ availableForInteraction), refer to the operation "authenticate-auto".
                throw SessionError.SESSION_EXPIRED.exception();
            }
            logger.warn("Triggering SMART_ID even though user is not available for interaction!");
        }

        // get "EE" from the Agent / provider
        AuthenticationResponse authenticationResponse =
                apiClient.authenticateDecoupled(userId, provider.getMarket(), personalId);

        sessionStorage.put(
                SwedbankBalticsConstants.AUTH_URL, authenticationResponse.getCollectAuthUri());

        // TODO: open smartId ???

        return AuthenticationStepResponse.executeNextStep();
    }

    @Override
    public String getIdentifier() {
        return "init_step";
    }
}
