package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps;

import com.google.common.util.concurrent.Uninterruptibles;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.AuthStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.AuthenticationStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.rpc.GenericResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.SwedbankBalticsApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.SwedbankBalticsConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.StepDataStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.SwedbankBalticsAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RequiredArgsConstructor
public class CollectStatusStep implements AuthenticationStep {

    private final SwedbankBalticsAuthenticator authenticator;
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final SwedbankBalticsApiClient apiClient;
    private final StepDataStorage stepDataStorage;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        final Credentials credentials = request.getCredentials();

        final String userId =
                authenticator.verifyCredentialsNotNullOrEmpty(
                        credentials.getField(Field.Key.USERNAME));

        String collectAuthUri = stepDataStorage.getAuthUrl();

        for (int i = 0; i < SwedbankBalticsConstants.SMART_ID_POLL_MAX_ATTEMPTS; i++) {
            AuthenticationStatusResponse authenticationStatusResponse;
            try {
                authenticationStatusResponse = collectAuthStatus(userId, collectAuthUri);
            } catch (HttpResponseException e) {
                throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception(e.getMessage());
            }

            if (authenticationStatusResponse.loginCanceled()) {
                throw ThirdPartyAppError.CANCELLED.exception();
            }

            String status = authenticationStatusResponse.getScaStatus();

            switch (status.toLowerCase()) {
                case AuthStatus.RECEIVED:
                case AuthStatus.STARTED:
                    break;
                case AuthStatus.FINALIZED:
                    stepDataStorage.putAuthCode(
                            authenticationStatusResponse.getAuthorizationCode());
                    return AuthenticationStepResponse.executeNextStep();
                case AuthStatus.FAILED:
                    throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
                default:
                    logger.warn("Unknown status {}", status);
                    throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
            }

            Uninterruptibles.sleepUninterruptibly(
                    SwedbankBalticsConstants.SMART_ID_POOL_FREQUENCY, TimeUnit.MILLISECONDS);
        }

        throw ThirdPartyAppError.TIMED_OUT.exception();
    }

    private AuthenticationStatusResponse collectAuthStatus(String userId, String collectAuthUri) {
        try {
            return apiClient.collectAuthStatus(userId, collectAuthUri);
        } catch (HttpResponseException e) {
            GenericResponse errorResponse = e.getResponse().getBody(GenericResponse.class);
            logger.warn(String.format("Can not collect status. Got error (%s)", errorResponse));
            throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
        }
    }

    @Override
    public String getIdentifier() {
        return "collect_status_step";
    }
}
