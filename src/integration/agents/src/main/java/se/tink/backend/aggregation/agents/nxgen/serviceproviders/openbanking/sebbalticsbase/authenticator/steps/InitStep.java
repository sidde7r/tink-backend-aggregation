package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.steps;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.SebBalticsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.SebBalticsCommonConstants.AuthStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.SebBalticsCommonConstants.PollValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.SebBalticsCommonConstants.ScaAuthMethods;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.SebBalticsCommonConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.SebBalticsDecoupledAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.rpc.AuthMethodSelectionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.rpc.DecoupledAuthMethod;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.rpc.DecoupledAuthRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.rpc.DecoupledAuthResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.configuration.SebBalticsConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RequiredArgsConstructor
@Slf4j
public class InitStep implements AuthenticationStep {

    private final SebBalticsDecoupledAuthenticator authenticator;
    private final SebBalticsBaseApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final SebBalticsConfiguration configuration;
    private final String bankBIC;
    private String authRequestId;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        final Credentials credentials = request.getCredentials();

        final String psuId = verifyCredentialsNotNullOrEmpty(credentials.getField(Key.USERNAME));

        final String psuCorporateId =
                verifyCredentialsNotNullOrEmpty(credentials.getField(Key.CORPORATE_ID));

        DecoupledAuthResponse authResponse =
                apiClient.startDecoupledAuthorization(
                        DecoupledAuthRequest.builder()
                                .psuId(psuId)
                                .clientId(configuration.getClientId())
                                .bic(bankBIC)
                                .psuCorporateId(psuCorporateId)
                                .build());

        authRequestId = authResponse.getAuthorizationId();

        sessionStorage.put(StorageKeys.AUTH_REQ_ID, authRequestId);

        AuthMethodSelectionResponse response =
                apiClient.updateDecoupledAuthStatus(
                        DecoupledAuthMethod.builder()
                                .chosenScaMethod(ScaAuthMethods.SMART_ID)
                                .build(),
                        authRequestId);

        authenticator.displayChallengeCodeToUser(response.getChallengeData().getCode());

        poll();

        return AuthenticationStepResponse.executeNextStep();
    }

    private void poll() throws AuthenticationException, AuthorizationException {
        String status = null;

        for (int i = 0; i < PollValues.SMART_ID_POLL_MAX_ATTEMPTS; i++) {
            status = apiClient.getDecoupledAuthStatus(authRequestId).getStatus();

            switch (status) {
                case AuthStatus.FINALIZED:
                    // SmartId/MobileId successful, proceed authentication
                    return;
                case AuthStatus.STARTED:
                    log.info("Authentication Started");
                    break;
                case AuthStatus.FAILED:
                    log.info("Authentication failed");
                    throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
                default:
                    log.warn(String.format("Unknown status (%s)", status));
                    throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
            }

            Uninterruptibles.sleepUninterruptibly(
                    PollValues.SMART_ID_POLL_FREQUENCY, TimeUnit.MILLISECONDS);
        }

        log.info(String.format("SmartId/ MobilId timed out internally, last status: %s", status));
    }

    public String verifyCredentialsNotNullOrEmpty(String credentials) throws LoginException {
        if (Strings.isNullOrEmpty(credentials) || credentials.trim().isEmpty()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        return credentials;
    }
}
