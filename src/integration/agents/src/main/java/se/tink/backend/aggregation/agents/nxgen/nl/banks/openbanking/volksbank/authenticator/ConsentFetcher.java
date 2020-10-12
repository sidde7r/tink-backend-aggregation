package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.configuration.VolksbankConfiguration;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class ConsentFetcher {

    private final VolksbankApiClient client;
    private final PersistentStorage persistentStorage;
    private final AgentConfiguration<VolksbankConfiguration> agentConfiguration;
    private final VolksbankConfiguration volksbankConfiguration;

    public ConsentFetcher(
            final VolksbankApiClient client,
            final PersistentStorage persistentStorage,
            AgentConfiguration<VolksbankConfiguration> agentConfiguration) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.agentConfiguration = agentConfiguration;
        this.volksbankConfiguration = agentConfiguration.getProviderSpecificConfiguration();
    }

    public String fetchConsent() {
        final String consentId =
                client.consentRequest(
                                new URL(agentConfiguration.getRedirectUrl()),
                                volksbankConfiguration.getClientId())
                        .getConsentId();

        persistentStorage.put(Storage.CONSENT, consentId);

        return consentId;
    }

    public boolean isConsentValid() {
        final String consentId = persistentStorage.get(Storage.CONSENT);

        // Consent is removed if we get "unsupported_grant_type" from Volksbank. The error we throw
        // is a bankservice error which puts the credential in temp error, this means that we try
        // to refresh it again. This is a band aid solution, we need to the overall logic for
        // consent errors (TC-3706).
        if (Strings.isNullOrEmpty(consentId)) {
            return false;
        }

        final String consentStatus =
                client.consentStatusRequest(volksbankConfiguration.getClientId(), consentId)
                        .getConsentStatus();

        return VolksbankConstants.ConsentStatuses.VALID.equalsIgnoreCase(consentStatus);
    }
}
