package se.tink.backend.aggregation.workers.encryption;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.libraries.credentials_requests.CredentialsRequest;
import se.tink.backend.aggregation.storage.database.models.CryptoConfiguration;
import se.tink.backend.aggregation.wrappers.CryptoWrapper;
import se.tink.libraries.cache.CacheClient;
import se.tink.libraries.cache.CacheScope;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CredentialsCrypto {
    private static final AggregationLogger logger = new AggregationLogger(CredentialsCrypto.class);

    private static final int CACHE_EXPIRE_TIME = Math.toIntExact(TimeUnit.MINUTES.toSeconds(20));


    private final CacheClient cacheClient;
    private final ControllerWrapper controllerWrapper;
    private final CryptoWrapper cryptoWrapper;

    public CredentialsCrypto(CacheClient cacheClient, ControllerWrapper controllerWrapper,
            CryptoWrapper cryptoWrapper) {
        this.cacheClient = cacheClient;
        this.controllerWrapper = controllerWrapper;
        this.cryptoWrapper = cryptoWrapper;
    }

    public boolean decrypt(CredentialsRequest request) {
        Credentials credentials = request.getCredentials();

        // See if there is any sensitive data in the cache
        String cachedSensitiveData = (String) cacheClient.get(CacheScope.ENCRYPTED_CREDENTIALS_BY_CREDENTIALSID,
                credentials.getId());

        // See if there is any sensitive data on the credential
        String credentialsSensitiveData = credentials.getSensitiveDataSerialized();

        if (Strings.isNullOrEmpty(cachedSensitiveData) && Strings.isNullOrEmpty(credentialsSensitiveData)) {
            // There's nothing to decrypt. Both cache and credential were empty.
            logger.error("There is nothing to decrypt.");
            return false;
        }

        String sensitiveData = pickMostRecentSensitiveData(cachedSensitiveData, credentialsSensitiveData);
        Preconditions.checkState(!Strings.isNullOrEmpty(sensitiveData), "Sensitive data was not set.");

        // Decrypt
        EncryptedCredentials encryptedCredentials = Preconditions.checkNotNull(
                SerializationUtils.deserializeFromString(sensitiveData, EncryptedCredentials.class));

        Optional<byte[]> key = cryptoWrapper.getCryptoKeyByKeyId(encryptedCredentials.getKeyId());

        if (!key.isPresent()) {
            logger.error(String.format("Could not find encryption key for %s:%d",
                    cryptoWrapper.getClientName(),
                    encryptedCredentials.getKeyId()));
            return false;
        }

        switch (encryptedCredentials.getVersion()) {
        case EncryptedCredentialsV1.VERSION:
            CredentialsCryptoV1.decryptCredential(key.get(), credentials, sensitiveData);
            break;
        default:
            // NYI.
            logger.error(String.format("EncryptedCredentials version not recognized: %d",
                    encryptedCredentials.getVersion()));
            return false;
        }

        return true;
    }

    public boolean encrypt(CredentialsRequest request, boolean doUpdateCredential) {
        Optional<CryptoConfiguration> cryptoConfiguration = cryptoWrapper.getLatestCryptoConfiguration();
        if (!cryptoConfiguration.isPresent()) {
            logger.error(String.format("Could not find crypto configuration %s", cryptoWrapper.getClientName()));
            return false;
        }

        int clusterKeyId = cryptoConfiguration.get().getCryptoConfigurationId().getKeyId();
        byte[] clusterKey = cryptoConfiguration.get().getDecodedKey();

        Credentials originalCredentials = request.getCredentials();
        Credentials sensitiveInformationCredentials = originalCredentials.clone();
        sensitiveInformationCredentials.onlySensitiveInformation(request.getProvider());

        // Always use the latest version, which is v1.
        EncryptedCredentialsV1 encryptedCredentials = CredentialsCryptoV1.encryptCredential(
                clusterKeyId,
                clusterKey,
                sensitiveInformationCredentials);

        String serializedEncryptedCredentials = encryptedCredentials.asSerialized();

        // Put the `serializedEncryptedCredentials` on the credential so that it can be passed back to the database
        // in createCredential() and updateCredential().
        originalCredentials.setSensitiveDataSerialized(serializedEncryptedCredentials);

        // Put the encrypted credentials into a temporary cache that is used by the DecryptCredentialsWorkerCommand.
        // It is possible that several refresh requests for the same credential to exists on the queue at the same time,
        // if the credentials are updated (such as usage of refresh token) the next request will have stale data.
        // Caching the credentials with a high enough CACHE_EXPIRE_TIME allows subsequent request to exchange its stale
        // data for the updated data in the cache.
        // This is not a permanent fix and should be resolved by overhauling the credential read/write logic such that
        // a request will never have stale data in the first place.
        // This cache is read in class `CredentialsCrypto`
        cacheClient.set(
                CacheScope.ENCRYPTED_CREDENTIALS_BY_CREDENTIALSID,
                request.getCredentials().getId(),
                CACHE_EXPIRE_TIME,
                serializedEncryptedCredentials);

        if (doUpdateCredential) {
            logger.info("Updating sensitive data");
            controllerWrapper.updateCredentialSensitive(request.getCredentials(), serializedEncryptedCredentials);
        }

        return true;
    }

    private String pickMostRecentSensitiveData(String a, String b) {
        // Return the other one if one is null.
        if (Strings.isNullOrEmpty(a)) {
            return b;
        } else if (Strings.isNullOrEmpty(b)) {
            return a;
        }

        // Return the latest one if both are set.
        EncryptedCredentials baseA = SerializationUtils.deserializeFromString(
                a, EncryptedCredentials.class);

        EncryptedCredentials baseB = SerializationUtils.deserializeFromString(
                b, EncryptedCredentials.class);

        if (baseA.getTimestamp().after(baseB.getTimestamp())) {
            return a;
        } else {
            return b;
        }
    }

}
