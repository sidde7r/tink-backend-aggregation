package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RequiredArgsConstructor
public class AccessCodeStorage {

    private static final String TMP_ACCESS_CODE_STORAGE_KEY = "tmp_oauth2_access_code";

    private final SessionStorage sessionStorage;

    public Optional<String> getAccessCodeFromSession() {
        return sessionStorage.get(TMP_ACCESS_CODE_STORAGE_KEY, String.class);
    }

    public void storeAccessCodeInSession(String accessCode) {
        sessionStorage.put(TMP_ACCESS_CODE_STORAGE_KEY, accessCode);
    }
}
