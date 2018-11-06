package se.tink.backend.aggregation.workers.encryption;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import se.tink.backend.aggregation.converter.HostConfigurationConverter;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.aggregationcontroller.AggregationControllerAggregationClient;
import se.tink.backend.aggregation.cluster.identification.ClusterId;
import se.tink.backend.aggregation.cluster.identification.ClusterInfo;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.common.cache.CacheScope;
import se.tink.backend.common.repository.mysql.aggregation.clustercryptoconfiguration.ClusterCryptoConfigurationRepository;
import se.tink.backend.core.ClusterCryptoConfiguration;
import se.tink.backend.core.CryptoId;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CredentialsCrypto {
    private static final AggregationLogger logger = new AggregationLogger(CredentialsCrypto.class);

    private final ClusterInfo clusterInfo;
    private final CacheClient cacheClient;
    private final ClusterCryptoConfigurationRepository clusterCryptoConfigurationRepository;
    private final AggregationControllerAggregationClient aggregationControllerAggregationClient;

    public CredentialsCrypto(ClusterInfo clusterInfo, CacheClient cacheClient,
            ClusterCryptoConfigurationRepository clusterCryptoConfigurationRepository,
            AggregationControllerAggregationClient aggregationControllerAggregationClient) {
        this.clusterInfo = clusterInfo;
        this.cacheClient = cacheClient;
        this.clusterCryptoConfigurationRepository = clusterCryptoConfigurationRepository;
        this.aggregationControllerAggregationClient = aggregationControllerAggregationClient;
    }

    private Optional<ClusterCryptoConfiguration> getClusterCryptoConfigurationFromClusterId(ClusterId clusterId) {
        // Get the most recent (keyId, key) for clusterId.getId()
        List<ClusterCryptoConfiguration> clusterCryptoConfigurations = clusterCryptoConfigurationRepository
                .findByCryptoIdClusterId(clusterId.getId());

        // Get the highest (most recent) keyId.
        return clusterCryptoConfigurations.stream().max(Comparator.comparing(t -> t.getCryptoId().getKeyId()));
    }

    private Optional<byte[]> getClusterKeyFromKeyId(ClusterId clusterId, int keyId) {
        CryptoId cryptoId = new CryptoId();
        cryptoId.setClusterId(clusterId.getId());
        cryptoId.setKeyId(keyId);
        ClusterCryptoConfiguration clusterCryptoConfiguration = clusterCryptoConfigurationRepository
                .findByCryptoId(cryptoId);

        if (Objects.isNull(clusterCryptoConfiguration)) {
            return Optional.empty();
        }

        return Optional.of(clusterCryptoConfiguration.getDecodedKey());
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

    public boolean decrypt(CredentialsRequest request) {
        Credentials credentials = request.getCredentials();

        // See if there is any sensitive data in the cache
        String cachedSensitiveData = (String)cacheClient.get(CacheScope.ENCRYPTED_CREDENTIALS_BY_CREDENTIALSID,
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

        Optional<byte[]> key = getClusterKeyFromKeyId(clusterInfo.getClusterId(), encryptedCredentials.getKeyId());
        if (!key.isPresent()) {
            logger.error(String.format("Could not find encryption key for %s:%d",
                    clusterInfo.getClusterId().getId(),
                    encryptedCredentials.getKeyId()));
            return false;
        }

        switch(encryptedCredentials.getVersion()) {
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
        Optional<ClusterCryptoConfiguration> clusterCryptoConfiguration = getClusterCryptoConfigurationFromClusterId(
                clusterInfo.getClusterId());
        if (!clusterCryptoConfiguration.isPresent()) {
            logger.error(String.format("Could not find crypto configuration %s", clusterInfo.getClusterId().getId()));
            return false;
        }

        int clusterKeyId = clusterCryptoConfiguration.get().getCryptoId().getKeyId();
        byte[] clusterKey = clusterCryptoConfiguration.get().getDecodedKey();

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
        cacheClient.set(
                CacheScope.ENCRYPTED_CREDENTIALS_BY_CREDENTIALSID,
                request.getCredentials().getId(),
                60 * 10,
                serializedEncryptedCredentials);

        if (doUpdateCredential) {
            logger.info("Updating sensitive data");
            aggregationControllerAggregationClient.updateCredentialSensitive(
                    HostConfigurationConverter.convert(clusterInfo),
                    request.getCredentials(),
                    serializedEncryptedCredentials);
        }

        return true;
    }
}
