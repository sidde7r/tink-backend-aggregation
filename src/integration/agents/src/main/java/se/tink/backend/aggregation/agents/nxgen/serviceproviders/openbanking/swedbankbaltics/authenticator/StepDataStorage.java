package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RequiredArgsConstructor
public class StepDataStorage {
    public static final String AUTH_URL = "auth_url";
    public static final String AUTH_CODE = "authorizationCode";
    public static final String ACC_RESP = "accountList";
    public static final String CONSENT_RESP = "consentResponse";

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

    public Optional<FetchAccountResponse> getAccountResponse() {
        return sessionStorage.get(ACC_RESP, FetchAccountResponse.class);
    }

    public void putAccountResponse(FetchAccountResponse resp) {
        sessionStorage.put(ACC_RESP, resp);
    }

    public Optional<ConsentResponse> getConsentResponse() {
        return sessionStorage.get(CONSENT_RESP, ConsentResponse.class);
    }

    public void putConsentResponse(ConsentResponse resp) {
        sessionStorage.put(CONSENT_RESP, resp);
    }
}
