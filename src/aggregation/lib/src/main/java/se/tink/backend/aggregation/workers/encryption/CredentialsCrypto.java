package se.tink.backend.aggregation.workers.encryption;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.FeatureFlags;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.storage.database.models.CryptoConfiguration;
import se.tink.backend.aggregation.wrappers.CryptoWrapper;
import se.tink.libraries.cache.CacheClient;
import se.tink.libraries.cache.CacheScope;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.encryptedpayload.EncryptedPayloadHead;
import se.tink.libraries.encryptedpayload.EncryptedPayloadV2;
import se.tink.libraries.encryptedpayload.VersionDeserializer;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CredentialsCrypto {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    // according to
    // https://grafana.global-production.tink.network/d/000000054/aggregation-service?editPanel=8&orgId=1&from=now-2d&to=now
    private static final int CACHE_EXPIRE_TIME = Math.toIntExact(TimeUnit.MINUTES.toSeconds(150));
    public static final MetricId CREDENTIALS_DECRYPT = MetricId.newId("credentials_decrypt");
    public static final MetricId CREDENTIALS_ENCRYPT = MetricId.newId("credentials_encrypt");

    private final CacheClient cacheClient;
    private final ControllerWrapper controllerWrapper;
    private final CryptoWrapper cryptoWrapper;
    private final MetricRegistry metricRegistry;

    public CredentialsCrypto(
            CacheClient cacheClient,
            ControllerWrapper controllerWrapper,
            CryptoWrapper cryptoWrapper,
            MetricRegistry metricRegistry) {
        this.cacheClient = cacheClient;
        this.controllerWrapper = controllerWrapper;
        this.cryptoWrapper = cryptoWrapper;
        this.metricRegistry = metricRegistry;
    }

    public boolean decrypt(CredentialsRequest request) {
        Credentials credentials = request.getCredentials();

        // See if there is any sensitive data in the cache
        String cachedSensitiveData =
                (String)
                        cacheClient.get(
                                CacheScope.ENCRYPTED_CREDENTIALS_BY_CREDENTIALSID,
                                credentials.getId());

        // See if there is any sensitive data on the credential
        String credentialsSensitiveData = credentials.getSensitiveDataSerialized();

        if (Strings.isNullOrEmpty(cachedSensitiveData)
                && Strings.isNullOrEmpty(credentialsSensitiveData)) {
            // There's nothing to decrypt. Both cache and credential were empty.
            logger.info("There is nothing to decrypt.");
            return true;
        }

        String sensitiveData =
                pickMostRecentSensitiveData(
                        cachedSensitiveData,
                        credentialsSensitiveData,
                        request.getProvider().getName());

        Preconditions.checkState(
                !Strings.isNullOrEmpty(sensitiveData), "Sensitive data was not set.");

        // Deserialize & Decrypt using right version
        return VersionDeserializer.withDefaultHandler(
                        head -> {
                            logger.error(
                                    String.format(
                                            "EncryptedCredentials version not recognized: %d",
                                            head.getVersion()));

                            cryptoMetrics(CREDENTIALS_DECRYPT, head, false);
                            return false;
                        })
                .setVersion1Handler(
                        v1 -> {
                            byte[] key = cryptoWrapper.getCryptoKeyByKeyId(v1.getKeyId());

                            try {
                                CredentialsCryptoV1.decryptCredential(key, credentials, v1);
                                cryptoMetrics(CREDENTIALS_DECRYPT, v1, true);
                            } catch (Exception e) {
                                logger.error(
                                        "Decryption failed for credentialsId {}",
                                        credentials.getId(),
                                        e);

                                cryptoMetrics(CREDENTIALS_DECRYPT, v1, false);
                                throw e;
                            }
                            return true;
                        })
                .setVersion2Handler(
                        v2 -> {
                            byte[] fieldsKey =
                                    cryptoWrapper.getCryptoKeyByKeyId(v2.getFields().getKeyId());
                            byte[] payloadKey =
                                    cryptoWrapper.getCryptoKeyByKeyId(v2.getPayload().getKeyId());

                            try {
                                CredentialsCryptoV2.decryptCredential(
                                        fieldsKey, payloadKey, credentials, v2);
                                cryptoMetrics(CREDENTIALS_DECRYPT, v2, true);
                            } catch (Exception e) {
                                logger.error(
                                        "Decryption failed for credentialsId {}",
                                        credentials.getId(),
                                        e);

                                cryptoMetrics(CREDENTIALS_DECRYPT, v2, false);
                                throw e;
                            }

                            return true;
                        })
                .handle(sensitiveData);
    }

    public boolean encrypt(CredentialsRequest request, boolean doUpdateCredential) {
        Optional<CryptoConfiguration> cryptoConfiguration =
                cryptoWrapper.getLatestCryptoConfiguration();
        if (!cryptoConfiguration.isPresent()) {
            logger.error(
                    String.format(
                            "Could not find crypto configuration %s",
                            cryptoWrapper.getClientName()));
            return false;
        }

        int clusterKeyId = cryptoConfiguration.get().getCryptoConfigurationId().getKeyId();
        byte[] clusterKey = cryptoConfiguration.get().getDecodedKey();

        Credentials originalCredentials = request.getCredentials();
        Credentials sensitiveInformationCredentials = originalCredentials.clone();
        sensitiveInformationCredentials.onlySensitiveInformation(request.getProvider());

        EncryptedPayloadHead encryptedCredentials;
        if (isAlphaUser(request) || isV2(request.getCredentials())) {
            // Upgrade to crypto V2 for new Aggregation Engine-based credentials revamp users
            encryptedCredentials =
                    CredentialsCryptoV2.encryptCredential(
                            clusterKeyId, clusterKey, sensitiveInformationCredentials);
        } else {
            // Regularly use V1
            encryptedCredentials =
                    CredentialsCryptoV1.encryptCredential(
                            clusterKeyId, clusterKey, sensitiveInformationCredentials);
        }

        String serializedEncryptedCredentials =
                SerializationUtils.serializeToString(encryptedCredentials);

        // Put the `serializedEncryptedCredentials` on the credential so that it can be passed back
        // to the database
        // in createCredential() and updateCredential().
        originalCredentials.setSensitiveDataSerialized(serializedEncryptedCredentials);

        // Put the encrypted credentials into a temporary cache that is used by the
        // DecryptCredentialsWorkerCommand.
        // It is possible that several refresh requests for the same credential to exists on the
        // queue at the same time,
        // if the credentials are updated (such as usage of refresh token) the next request will
        // have stale data.
        // Caching the credentials with a high enough CACHE_EXPIRE_TIME allows subsequent request to
        // exchange its stale
        // data for the updated data in the cache.
        // This is not a permanent fix and should be resolved by overhauling the credential
        // read/write logic such that
        // a request will never have stale data in the first place.
        // This cache is read in class `CredentialsCrypto`
        cacheClient.set(
                CacheScope.ENCRYPTED_CREDENTIALS_BY_CREDENTIALSID,
                request.getCredentials().getId(),
                CACHE_EXPIRE_TIME,
                serializedEncryptedCredentials);

        cryptoMetrics(CREDENTIALS_ENCRYPT, encryptedCredentials, true);

        if (doUpdateCredential) {
            logger.info("Updating sensitive data");
            controllerWrapper.updateCredentialSensitive(
                    request.getCredentials(),
                    serializedEncryptedCredentials,
                    request.getOperationId());
        }

        return true;
    }

    private void cryptoMetrics(
            MetricId metricId, EncryptedPayloadHead encryptedCredentials, boolean success) {
        metricRegistry
                .meter(
                        metricId.label("success", success)
                                .label("version", encryptedCredentials.getVersion()))
                .inc();
    }

    private boolean isV2(Credentials credentials) {
        EncryptedPayloadHead previous =
                SerializationUtils.deserializeFromString(
                        credentials.getSensitiveDataSerialized(), EncryptedPayloadHead.class);

        return previous != null && previous.getVersion() == EncryptedPayloadV2.VERSION;
    }

    private boolean isAlphaUser(CredentialsRequest request) {
        final List<String> flags = request.getUser().getFlags();
        if (flags == null) return false;

        return flags.contains(FeatureFlags.ALPHA_TEST_CREDENTIALS_REVAMP)
                || flags.contains(FeatureFlags.TINK_EMPLOYEE)
                || flags.contains(FeatureFlags.IOS_BETA)
                || flags.contains(FeatureFlags.ANDROID_BETA)
                || flags.contains(FeatureFlags.FRAUD_PROTECTION);
    }

    private String pickMostRecentSensitiveData(String a, String b, String provider) {
        // Return the other one if one is null.
        if (Strings.isNullOrEmpty(a)) {
            return b;
        } else if (Strings.isNullOrEmpty(b)) {
            return a;
        }

        // Return the latest one if both are set.
        EncryptedPayloadHead baseA =
                SerializationUtils.deserializeFromString(a, EncryptedPayloadHead.class);

        EncryptedPayloadHead baseB =
                SerializationUtils.deserializeFromString(b, EncryptedPayloadHead.class);

        try {
            if (baseB.getTimestamp() == null || baseA.getTimestamp().after(baseB.getTimestamp())) {
                return a;
            } else {
                return b;
            }
        } catch (NullPointerException e) {
            logger.warn("{DEBUG_NPE}: provider: " + provider);
            logger.warn("{DEBUG_NPE}: a: " + a, e);
            logger.warn("{DEBUG_NPE}: b: " + b, e);
            throw e;
        }
    }
}
