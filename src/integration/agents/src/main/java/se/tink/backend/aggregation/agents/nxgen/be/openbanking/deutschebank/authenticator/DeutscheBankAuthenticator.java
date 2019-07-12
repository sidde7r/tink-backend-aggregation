package se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.authenticator;

import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.DeutscheBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.DeutscheBankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.configuration.BerlinGroupConfiguration;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class DeutscheBankAuthenticator {

    private final DeutscheBankApiClient apiClient;
    private final SessionStorage sessionStorage;
    private BerlinGroupConfiguration configuration;
    private String iban;

    public DeutscheBankAuthenticator(
            DeutscheBankApiClient apiClient,
            SessionStorage sessionStorage,
        BerlinGroupConfiguration configuration,
            String iban) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.configuration = configuration;
        this.iban = iban;
    }

    public URL buildAuthorizeUrl(String state) {
        ConsentBaseResponse consent = apiClient.getConsent(state, iban);

        sessionStorage.put(StorageKeys.CONSENT_ID, consent.getConsentId());
        return new URL(consent.getLinks().getScaRedirect().getHref());
    }
}
