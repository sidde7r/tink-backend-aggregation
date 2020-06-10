package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.hmac;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessTokenStorage;
import se.tink.backend.aggregation.nxgen.core.authentication.HmacMultiToken;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RequiredArgsConstructor
public class HmacMultiTokenStorage implements AccessTokenStorage<HmacMultiToken> {

    private static final String HMAC_MULTI_TOKEN_KEY = "hmac_multi_token";
    private static final String TMP_HMAC_TOKEN_STORAGE_KEY = "tmp_hmac_multi_token";

    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;

    @Override
    public Optional<HmacMultiToken> getToken() {
        return persistentStorage.get(HMAC_MULTI_TOKEN_KEY, HmacMultiToken.class);
    }

    @Override
    public Optional<HmacMultiToken> getTokenFromSession() {
        return sessionStorage.get(TMP_HMAC_TOKEN_STORAGE_KEY, HmacMultiToken.class);
    }

    @Override
    public void storeToken(HmacMultiToken hmacMultiToken) {
        persistentStorage.put(HMAC_MULTI_TOKEN_KEY, hmacMultiToken);
    }

    @Override
    public void storeTokenInSession(HmacMultiToken hmacMultiToken) {
        sessionStorage.put(TMP_HMAC_TOKEN_STORAGE_KEY, hmacMultiToken);
    }

    @Override
    public void rotateToken(HmacMultiToken hmacMultiToken) {
        persistentStorage.rotateStorageValue(HMAC_MULTI_TOKEN_KEY, hmacMultiToken);
    }

    @Override
    public void clearToken() {
        persistentStorage.remove(HMAC_MULTI_TOKEN_KEY);
    }
}
