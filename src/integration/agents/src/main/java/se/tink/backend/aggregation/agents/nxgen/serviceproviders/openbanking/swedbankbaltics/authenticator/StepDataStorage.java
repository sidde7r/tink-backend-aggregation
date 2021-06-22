package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RequiredArgsConstructor
public class StepDataStorage {
    public static final String AUTH_URL = "auth_url";
    public static final String AUTH_CODE = "authorizationCode";

    private final SessionStorage sessionStorage;

    public String getAuthUrl() {
        return sessionStorage.get(AUTH_URL);
    }

    public void putAuthUrl(String url) {
        sessionStorage.put(AUTH_URL, url);
    }

    public String getAuthCode() {
        return sessionStorage.get(AUTH_CODE);
    }

    public void putAuthCode(String code) {
        sessionStorage.put(AUTH_CODE, code);
    }
}
