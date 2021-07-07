package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.entities.TokenEntity;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;

/**
 * This is needed due to Bunq's header signing requirement, see https://doc.bunq.com/#/signing
 * depending if we are making calls as a PSD2Provider or on behalf of the user we have to use
 * different keys to sign the "X-Bunq-Client-Signature" header so when we update the client
 * authentication token, we need to update which key should be use to sign the client signature
 * header. Depending on which role we are making the calls as and also on which phase of the
 * authentication flow we are we should use different client authentication tokens, i.e. to register
 * a device and start a session we have to use the token that we got from the installation call
 * while any call done after a session is started should use the token received from in the session
 * response as the client authentication token.
 */
@RequiredArgsConstructor
public class BunqClientAuthTokenHandler {
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;
    private final TemporaryStorage temporaryStorage;

    public void updateClientAuthToken(String storageTokenKey) {
        TokenEntity newClientAuthToken =
                persistentStorage
                        .get(storageTokenKey, TokenEntity.class)
                        .orElseThrow(
                                () -> new IllegalStateException("No client auth token found."));
        sessionStorage.put(BunqBaseConstants.StorageKeys.CLIENT_AUTH_TOKEN, newClientAuthToken);
        String storageDeviceRSASigningKeyPairKey;
        switch (storageTokenKey) {
            case BunqConstants.StorageKeys.PSD2_CLIENT_AUTH_TOKEN:
                storageDeviceRSASigningKeyPairKey =
                        BunqConstants.StorageKeys.PSD2_DEVICE_RSA_SIGNING_KEY_PAIR;
                break;

            case BunqBaseConstants.StorageKeys.USER_CLIENT_AUTH_TOKEN:
                storageDeviceRSASigningKeyPairKey =
                        BunqBaseConstants.StorageKeys.USER_DEVICE_RSA_SIGNING_KEY_PAIR;
                break;

            default:
                throw new IllegalArgumentException(
                        "Invalid storageToken key : "
                                + storageTokenKey
                                + " should be one of [BunqBaseConstants.StorageKeys.USER_CLIENT_AUTH_TOKEN, BunqConstants.StorageKeys.PSD2_CLIENT_AUTH_TOKEN]");
        }

        temporaryStorage.put(
                newClientAuthToken.getToken(),
                persistentStorage.get(storageDeviceRSASigningKeyPairKey));
    }
}
