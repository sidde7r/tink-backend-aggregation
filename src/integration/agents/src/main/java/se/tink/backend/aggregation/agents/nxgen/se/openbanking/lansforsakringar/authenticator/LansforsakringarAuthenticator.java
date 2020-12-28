package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarStorageHelper;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
@Slf4j
public class LansforsakringarAuthenticator implements OAuth2Authenticator {

    private final LansforsakringarApiClient apiClient;
    private final LansforsakringarStorageHelper storageHelper;

    @Override
    public URL buildAuthorizeUrl(String state) {
        ConsentResponse consent = apiClient.getConsent();
        storageHelper.setConsentId(consent.getConsentId());

        return apiClient.buildAuthorizeUrl(state, consent.getAuthorisationId());
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        return apiClient.exchangeAuthorizationCode(code);
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws BankServiceException, SessionException {
        return apiClient.refreshToken(refreshToken);
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        // will use token from persistent storage
    }

    @Override
    public void handleSpecificCallbackDataError(Map<String, String> callbackData)
            throws AuthenticationException {
        // LF wont provide with an error parameter on a canceled callback. Instead they provide with
        // an (empty) pickup callback parameter
        final String callbackParameter =
                callbackData.getOrDefault(LansforsakringarConstants.CallbackParam.PICKUP, null);
        if (callbackParameter != null) {
            throw ThirdPartyAppError.CANCELLED.exception();
        }
    }

    public boolean isConsentValid() {
        try {
            if (apiClient.getConsentStatus() == null) {
                return false;
            }
            return apiClient.getConsentStatus().isConsentValid();
        } catch (HttpResponseException e) {
            log.error("Bank thrown an error when fetching consent status", e);
            throw SessionError.SESSION_EXPIRED.exception();
        } catch (RuntimeException e) {
            log.error("Unexpected error occurred when fetching consent status.", e);
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
