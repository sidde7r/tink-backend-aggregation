package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.AuthStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.AuthenticationStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.SwedbankBalticsApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.SwedbankBalticsConstants.Steps;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.SwedbankBalticsConstants.TimeValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.StepDataStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.SwedbankBalticsAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
@RequiredArgsConstructor
public class CollectStatusStep implements AuthenticationStep {

    private final SwedbankBalticsAuthenticator authenticator;
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

        // we send challenge code that we received in response to user, user have to compare this
        // code and code in SMART_ID
        authenticator.displayChallengeCodeToUser(stepDataStorage.getChallengeCode());

        for (int i = 0; i < TimeValues.SMART_ID_POLL_MAX_ATTEMPTS; i++) {
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
                    log.warn("Unknown status {}", status);
                    throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
            }

            Uninterruptibles.sleepUninterruptibly(
                    TimeValues.SMART_ID_POLL_FREQUENCY, TimeUnit.MILLISECONDS);
        }

        throw ThirdPartyAppError.TIMED_OUT.exception();
    }

    private AuthenticationStatusResponse collectAuthStatus(String userId, String collectAuthUri) {
        try {
            return apiClient.collectBalticAuthStatus(userId, collectAuthUri);
        } catch (HttpResponseException e) {
            log.error(
                    "Can not collect status. Status code {}, body {}",
                    e.getResponse().getStatus(),
                    e.getResponse().getBody(String.class));
            throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
        }
    }

    @Override
    public String getIdentifier() {
        return Steps.COLLECT_STATUS_STEP;
    }
}
