package se.tink.backend.aggregation.agents.nxgen.se.other.csn;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RequiredArgsConstructor
public class CSNAuthSessionStorageHelper {
    private final SessionStorage sessionStorage;

    public void storeAccessToken(String accessToken) {
        sessionStorage.put(CSNConstants.Storage.ACCESS_TOKEN, accessToken);
    }

    public Optional<String> getAccessToken() {
        return Optional.ofNullable(sessionStorage.get(CSNConstants.Storage.ACCESS_TOKEN));
    }
}
