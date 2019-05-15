package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.granitbank.authenticator;

import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.granitbank.GranitBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.granitbank.configuration.GranitBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.authenticator.FintechblocksAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class GranitBankAuthenticator extends FintechblocksAuthenticator {

    public GranitBankAuthenticator(
            GranitBankApiClient apiClient,
            PersistentStorage persistentStorage,
            GranitBankConfiguration configuration) {
        super(apiClient, persistentStorage, configuration);
    }
}
