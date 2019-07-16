package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.authenticator;

import com.google.common.base.Preconditions;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.BpceGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.BpceGroupConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BpceGroupAuthenticator implements Authenticator {

    private final BpceGroupApiClient apiClient;
    private final PersistentStorage persistentStorage;

    public BpceGroupAuthenticator(
            BpceGroupApiClient apiClient, PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public void authenticate(Credentials credentials) {
        TokenResponse tokenResponse = apiClient.authenticate();

        persistentStorage.put(
                StorageKeys.ACCESS_TOKEN,
                Preconditions.checkNotNull(tokenResponse.getAccessToken()));
    }
}
