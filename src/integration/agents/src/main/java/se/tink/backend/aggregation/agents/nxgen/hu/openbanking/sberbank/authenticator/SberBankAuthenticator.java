package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.sberbank.authenticator;

import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.sberbank.SberBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.sberbank.configuration.SberBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.authenticator.FintechblocksAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SberBankAuthenticator extends FintechblocksAuthenticator {

    public SberBankAuthenticator(
            SberBankApiClient apiClient,
            PersistentStorage persistentStorage,
            SberBankConfiguration configuration) {
        super(apiClient, persistentStorage, configuration);
    }
}
