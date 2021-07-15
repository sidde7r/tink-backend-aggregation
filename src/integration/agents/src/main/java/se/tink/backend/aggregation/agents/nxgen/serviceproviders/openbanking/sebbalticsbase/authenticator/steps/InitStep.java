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
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.SebBalticsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.SebBalticsCommonConstants.StorageKeys;
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

    private final SebBalticsBaseApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final SebBalticsConfiguration configuration;
    private String authRequestId;
    private String psuId;
    private String psuCorporateId;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        final Credentials credentials = request.getCredentials();

        if (credentials.hasField(Key.USERNAME)) {
            psuId = credentials.getField(Key.USERNAME);
            if (Strings.isNullOrEmpty(psuId)) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }
        }

        if (credentials.hasField(Key.CORPORATE_ID)) {
            psuCorporateId = credentials.getField(Key.CORPORATE_ID);
            if (Strings.isNullOrEmpty(psuCorporateId)) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }
        }

        DecoupledAuthResponse authResponse =
                apiClient.startDecoupledAuthorization(
                        DecoupledAuthRequest.builder()
                                .psuId(psuId)
                                .clientId(configuration.getClientId())
                                .bic(apiClient.getBic())
                                .psuCorporateId(psuCorporateId)
                                .build());

        authRequestId = authResponse.getAuthorizationId();

        sessionStorage.put(StorageKeys.AUTH_REQ_ID, authRequestId);

        apiClient.updateDecoupledAuthStatus(
                DecoupledAuthMethod.builder().chosenScaMethod("SmartID").build(), authRequestId);

        poll();

        return AuthenticationStepResponse.executeNextStep();
    }

    private void poll() throws AuthenticationException, AuthorizationException {
        String status = null;

        for (int i = 0; i < 90; i++) {
            status = apiClient.getDecoupledAuthStatus(authRequestId).getStatus();

            switch (status) {
                case "finalized":
                    // SmartId/MobileId successful, proceed authentication
                    return;
                case "started":
                    log.info("Authentication Started");
                    break;
                case "failed":
                    log.info("Authentication failed");
                    throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
                default:
                    log.warn(String.format("Unknown status (%s)", status));
                    throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
            }

            Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
        }

        log.info(String.format("SmartId/ MobilId timed out internally, last status: %s", status));
    }
}
