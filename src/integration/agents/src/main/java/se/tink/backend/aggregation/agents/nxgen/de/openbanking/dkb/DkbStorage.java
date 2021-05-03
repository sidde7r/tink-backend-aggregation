package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb;

import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
@RequiredArgsConstructor
public class DkbStorage {

    private static final String JSESSIONID_KEY = "JSESSIONID_KEY";
    private static final String XSRF_TOKEN_KEY = "XSRF_TOKEN_KEY";
    private static final String ACCESS_TOKEN_KEY = "ACCESS_TOKEN_KEY";
    private static final String CONSENT_ID_KEY = "CONSENT_KEY";
    private static final String FIRST_FETCH_FLAG = "firstFetch";
    private static final String DONE = "done";

    private final PersistentStorage persistentStorage;

    public void setJsessionid(String jsessionid) {
        persistentStorage.put(JSESSIONID_KEY, jsessionid);
    }

    public String getJsessionid() {
        return persistentStorage
                .get(JSESSIONID_KEY, String.class)
                .orElseThrow(() -> new NoSuchElementException("Can't obtain stored JSESSIONID."));
    }

    public void setXsrfToken(String xsrfToken) {
        persistentStorage.put(XSRF_TOKEN_KEY, xsrfToken);
    }

    public String getXsrfToken() {
        return persistentStorage
                .get(XSRF_TOKEN_KEY, String.class)
                .orElseThrow(() -> new NoSuchElementException("Can't obtain stored XSRF token."));
    }

    public void setAccessToken(OAuth2Token accessToken) {
        persistentStorage.put(ACCESS_TOKEN_KEY, accessToken);
    }

    public Optional<OAuth2Token> getAccessToken() {
        return persistentStorage.get(ACCESS_TOKEN_KEY, OAuth2Token.class);
    }

    public void setConsentId(String consent) {
        persistentStorage.put(CONSENT_ID_KEY, consent);
    }

    public Optional<String> getConsentId() {
        return persistentStorage.get(CONSENT_ID_KEY, String.class);
    }

    public boolean isFirstFetch() {
        return !DONE.equals(persistentStorage.get(FIRST_FETCH_FLAG));
    }

    public void markFirstFetchAsDone() {
        persistentStorage.put(FIRST_FETCH_FLAG, DONE);
    }
}
