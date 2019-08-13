package se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken.HandelsbankenSEConstants.OAuth2Type;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Scope;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class HandelsbankenSEAuthenticator implements Authenticator {

    private final HandelsbankenBaseApiClient apiClient;
    private final PersistentStorage persistentStorage;

    public HandelsbankenSEAuthenticator(
            HandelsbankenBaseApiClient apiClient, PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public void authenticate(Credentials credentials) {
        TokenResponse response = apiClient.requestClientCredentialGrantTokenWithScope(Scope.PIS);

        // TODO: Try to avoid storing this token. Maybe put it in the api client and use
        //         it right
        // away and not store it in the storage
        persistentStorage.put(
                PersistentStorageKeys.OAUTH_2_TOKEN,
                OAuth2Token.create(
                        OAuth2Type.BEARER,
                        response.getAccessToken(),
                        null,
                        response.getExpiresIn()));
    }
}
