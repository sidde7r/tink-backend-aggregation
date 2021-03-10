package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.storage;

import java.util.Optional;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.Storage;

public class DemobankStorage {

    private static final String ACCESS_TOKEN_KEY = "ACCESS_TOKEN_KEY";
    private static final String PAYMENT_ID_KEY = "PAYMENT_ID_KEY";
    private static final String AUTHORIZE_URL_KEY = "AUTHORIZE_URL_KEY";

    private final Storage storage = new Storage();

    public String getPaymentId() {
        return storage.get(PAYMENT_ID_KEY, String.class)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "PaymentId has not been found in the storage"));
    }

    public void storePaymentId(String paymentId) {
        storage.put(PAYMENT_ID_KEY, paymentId);
    }

    public Optional<OAuth2Token> getAccessToken() {
        return storage.get(ACCESS_TOKEN_KEY, OAuth2Token.class);
    }

    public void storeAccessToken(OAuth2Token accessToken) {
        storage.put(ACCESS_TOKEN_KEY, accessToken);
    }

    public String getAuthorizeUrl() {
        return storage.get(AUTHORIZE_URL_KEY, String.class)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Authorize Url has not been found in the storage"));
    }

    public void storeAuthorizeUrl(String authorizeUrl) {
        storage.put(AUTHORIZE_URL_KEY, authorizeUrl);
    }
}
