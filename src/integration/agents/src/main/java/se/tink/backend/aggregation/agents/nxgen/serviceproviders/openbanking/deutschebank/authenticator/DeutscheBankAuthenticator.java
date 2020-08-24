package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc.ConsentBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class DeutscheBankAuthenticator {

    private final DeutscheBankApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final String iban;
    private final String psuId;

    public DeutscheBankAuthenticator(
            DeutscheBankApiClient apiClient,
            SessionStorage sessionStorage,
            String iban,
            String psuId) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.iban = iban;
        this.psuId = psuId;
    }

    public URL authenticate(String state) {
        ConsentBaseResponse consent = apiClient.getConsent(state, iban, psuId);
        sessionStorage.put(StorageKeys.CONSENT_ID, consent.getConsentId());
        return new URL(consent.getLinks().getScaRedirect().getHref());
    }

    public void confirmAuthentication() {
        Optional.ofNullable(apiClient.getConsentStatus())
                .map(ConsentStatusResponse::getConsentStatus)
                .filter(
                        consentStatus ->
                                consentStatus.equals(DeutscheBankConstants.StatusValues.VALID))
                .orElseThrow(LoginError.CREDENTIALS_VERIFICATION_ERROR::exception);
    }
}
