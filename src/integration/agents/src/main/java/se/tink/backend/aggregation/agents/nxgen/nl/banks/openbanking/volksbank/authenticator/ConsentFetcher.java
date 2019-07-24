package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator;

import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class ConsentFetcher {

    private final VolksbankApiClient client;
    private final PersistentStorage persistentStorage;
    private final boolean isSandbox;

    public ConsentFetcher(
            final VolksbankApiClient client,
            final PersistentStorage persistentStorage,
            final boolean isSandbox) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.isSandbox = isSandbox;
    }

    /** @return A consent ID, either by asking the bank for it or by getting it from storage. */
    public String fetchConsent() {
        final String consentId;
        if (persistentStorage.containsKey(Storage.CONSENT)) {
            consentId = persistentStorage.get(Storage.CONSENT);
        } else {
            if (isSandbox) {
                // Sandbox behaves a bit differently
                final String consentResponseString = client.consentRequestString();
                consentId =
                        "SNS" + StringUtils.substringBetween(consentResponseString, "\"SNS", "\"");
            } else {
                final ConsentResponse consent = client.consentRequest();
                consentId = consent.getConsentId();
            }
            persistentStorage.put(Storage.CONSENT, consentId);
        }
        return consentId;
    }
}
