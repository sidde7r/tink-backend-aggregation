package se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.authenticator;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.RedsysApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.RedsysConstants;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.RedsysConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.RedsysConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.configuration.RedsysConfiguration;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class RedsysAuthenticator implements OAuth2Authenticator {

    private final RedsysApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final RedsysConfiguration configuration;
    private final String codeVerifier;

    public RedsysAuthenticator(
            RedsysApiClient apiClient,
            PersistentStorage persistentStorage,
            RedsysConfiguration configuration) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.configuration = configuration;
        this.codeVerifier = generateCodeVerifier();
    }

    private RedsysConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return apiClient.getAuthorizeUrl(state, this.codeVerifier);
    }

    private boolean storedConsentIsValid(OAuth2Token token) {
        String consentId = persistentStorage.get(StorageKeys.CONSENT_ID);
        if (Strings.isNullOrEmpty(consentId)) {
            return false;
        }
        return apiClient.getConsentStatus(consentId, token).isValid();
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) {
        OAuth2Token token = apiClient.getToken(code, this.codeVerifier);
        if (storedConsentIsValid(token)) {
            return token;
        }

        // FIXME: ugly hack for testing, handle in an AuthenticationController
        // request consent
        String consentUrl = apiClient.requestConsent(token);
        String consentId = persistentStorage.get(StorageKeys.CONSENT_ID);
        RedsysConstants.ConsentStatus consentStatus = null;
        System.out.println("CONSENT REDIRECT: " + consentUrl);

        // wait for consent approval
        for (int i = 0; i < 10; i++) {
            consentStatus = apiClient.getConsentStatus(consentId, token);
            if (consentStatus.isValid()) {
                // got consent
                return token;
            } else if (consentStatus.isReceived()) {
                // wait some more
                System.out.println("Waiting for consent acceptance");
                Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
            } else {
                // other status
                break;
            }
        }

        throw new IllegalStateException("Did not get consent: " + consentStatus.toString());
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) throws SessionException {
        return apiClient.refreshToken(refreshToken);
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, accessToken);
    }

    private String generateCodeVerifier() {
        return RandomUtils.generateRandomBase64UrlEncoded(48);
    }
}
