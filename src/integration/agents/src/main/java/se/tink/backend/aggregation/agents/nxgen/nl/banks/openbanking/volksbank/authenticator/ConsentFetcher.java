package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator;

import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.configuration.VolksbankConfiguration;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class ConsentFetcher {

    private final VolksbankApiClient client;
    private final PersistentStorage persistentStorage;
    private final boolean isSandbox;
    private final VolksbankConfiguration configuration;

    public ConsentFetcher(
            final VolksbankApiClient client,
            final PersistentStorage persistentStorage,
            final boolean isSandbox,
            final VolksbankConfiguration configuration) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.isSandbox = isSandbox;
        this.configuration = configuration;
    }

    /** @return A consent ID, either by asking the bank for it or by getting it from storage. */
    public String fetchConsent() {
        final String consentId;
        if (persistentStorage.containsKey(Storage.CONSENT)) {
            consentId = persistentStorage.get(Storage.CONSENT);
        } else {
            final URL redirectUrl = configuration.getAisConfiguration().getRedirectUrl();
            final String clientId = configuration.getAisConfiguration().getClientId();

            if (isSandbox) {
                // Sandbox behaves a bit differently
                final String consentResponseString =
                        client.consentRequestString(redirectUrl, clientId);
                consentId =
                        "SNS" + StringUtils.substringBetween(consentResponseString, "\"SNS", "\"");
            } else {
                final ConsentResponse consent = client.consentRequest(redirectUrl, clientId);
                consentId = consent.getConsentId();
            }
            persistentStorage.put(Storage.CONSENT, consentId);
        }
        return consentId;
    }
}
