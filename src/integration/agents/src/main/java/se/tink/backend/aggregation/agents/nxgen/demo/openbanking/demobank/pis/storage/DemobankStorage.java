package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.storage;

import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.ErrorMessages;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.Storage;

public class DemobankStorage {

    private static final String ACCESS_TOKEN_KEY = "ACCESS_TOKEN_KEY";
    private static final String PAYMENT_ID_KEY = "PAYMENT_ID_KEY";
    private static final String AUTHORIZE_URL_KEY = "AUTHORIZE_URL_KEY";
    private static final String EMBEDDED_AUTHORIZE_URL_KEY = "EMBEDDED_AUTHORIZE_URL_KEY";

    private final Storage storage = new Storage();

    public String getPaymentId() {
        return storage.get(PAYMENT_ID_KEY, String.class)
                .orElseThrow(
                        () -> new IllegalArgumentException(ErrorMessages.PAYMENT_ID_NOT_FOUND));
    }

    public void storePaymentId(String paymentId) {
        storage.put(PAYMENT_ID_KEY, paymentId);
    }

    public OAuth2Token getAccessToken() {
        return storage.get(ACCESS_TOKEN_KEY, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalArgumentException(ErrorMessages.OAUTH_TOKEN_NOT_FOUND));
    }

    public void storeAccessToken(OAuth2Token accessToken) {
        storage.put(ACCESS_TOKEN_KEY, accessToken);
    }

    public String getAuthorizeUrl() {
        return storage.get(AUTHORIZE_URL_KEY, String.class)
                .orElseThrow(
                        () -> new IllegalArgumentException(ErrorMessages.AUTHORIZE_URL_NOT_FOUND));
    }

    public void storeAuthorizeUrl(String authorizeUrl) {
        storage.put(AUTHORIZE_URL_KEY, authorizeUrl);
    }

    public String getEmbeddedAuthorizeUrl() {
        return storage.get(EMBEDDED_AUTHORIZE_URL_KEY, String.class)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        ErrorMessages.EMBEDDED_AUTHORIZE_URL_NOT_FOUND));
    }

    public void storeEmbeddedAuthorizeUrl(String embeddedAuthorizeUrl) {
        storage.put(EMBEDDED_AUTHORIZE_URL_KEY, embeddedAuthorizeUrl);
    }
}
