package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.authenticator;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvConstants.Values;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementalWaitRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.constants.ThirdPartyAppConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.ThirdPartyAppRequestParamsProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
@Slf4j
public class LhvThirdPartyAppRequestParamsProvider implements ThirdPartyAppRequestParamsProvider {

    private final StrongAuthenticationState strongAuthenticationState;
    private final Credentials credentials;
    private final PersistentStorage persistentStorage;

    @Override
    public ThirdPartyAppAuthenticationPayload getPayload() {
        return ThirdPartyAppAuthenticationPayload.of(
                new URL(persistentStorage.get(StorageKeys.CONSENT_STATUS_URL)));
    }

    @Override
    public SupplementalWaitRequest getWaitingConfiguration() {
        return new SupplementalWaitRequest(
                strongAuthenticationState.getSupplementalKey(),
                ThirdPartyAppConstants.WAIT_FOR_MINUTES,
                TimeUnit.MINUTES);
    }

    AuthenticationStepResponse processThirdPartyCallback(Map<String, String> callbackData) {
        if (callbackData.get(HeaderValues.OK).equalsIgnoreCase(String.valueOf(false))) {
            throw SessionError.CONSENT_INVALID.exception();
        } else {
            credentials.setSessionExpiryDate(LocalDateTime.now().plusDays(Values.HISTORY_MAX_DAYS));
            return AuthenticationStepResponse.authenticationSucceeded();
        }
    }
}
