package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.mkb.authenticator;

import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.mkb.MkbApiClient;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.mkb.configuration.MkbConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.authenticator.FintechblocksAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class MkbAuthenticator extends FintechblocksAuthenticator {

    public MkbAuthenticator(
            MkbApiClient apiClient,
            PersistentStorage persistentStorage,
            MkbConfiguration configuration) {
        super(apiClient, persistentStorage, configuration);
    }
}
