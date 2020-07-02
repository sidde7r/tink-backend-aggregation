package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.storage;

import java.util.Optional;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@AllArgsConstructor
public class N26Storage {

    private static final String ACCESS_TOKEN = "accessToken";
    private static final String EXPIRES_AT = "expiresAt";

    private final PersistentStorage persistentStorage;

    public void storeAccessToken(String token) {
        persistentStorage.put(ACCESS_TOKEN, token);
    }

    public String getAccessToken() {
        return persistentStorage.get(ACCESS_TOKEN);
    }

    public void storeAccessTokenExpiryDate(Long expiryDateTime) {
        persistentStorage.put(EXPIRES_AT, expiryDateTime);
    }

    public Optional<Long> getAccessTokenExpiryDate() {
        return persistentStorage.get(EXPIRES_AT, Long.class);
    }

    public void clear() {
        persistentStorage.put(ACCESS_TOKEN, StringUtils.EMPTY);
        persistentStorage.put(EXPIRES_AT, StringUtils.EMPTY);
    }
}
