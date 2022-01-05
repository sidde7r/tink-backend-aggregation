package se.tink.backend.aggregation.workers.encryption;

import com.google.common.base.Strings;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import net.spy.memcached.internal.OperationFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.storage.database.models.CryptoConfiguration;
import se.tink.backend.aggregation.wrappers.CryptoWrapper;
import se.tink.credentials.libraries.encryption.versions.CredentialsCryptoV1;
import se.tink.credentials.libraries.encryption.versions.CredentialsCryptoV2;
import se.tink.libraries.cache.CacheClient;
import se.tink.libraries.cache.CacheScope;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.encryptedpayload.EncryptedPayloadHead;
import se.tink.libraries.encryptedpayload.VersionDeserializer;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CredentialsCrypto {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    // according to
    // https://grafana.global-production.tink.network/d/000000054/aggregation-service?editPanel=8&orgId=1&from=now-2d&to=now
    // some refreshes stopped before finalizing DecryptCredentialsWorkerCommand or 500 exception pop
    // up when finalizing it
    // which causes the sensitive data to not persistent back to the customer cluster
    private static final int CACHE_EXPIRE_TIME = Math.toIntExact(TimeUnit.MINUTES.toSeconds(1500));
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

    public boolean decrypt(CredentialsRequest request, Charset charset) {
        Credentials credentials = request.getCredentials();

        // See if there is any sensitive data on the credential
        String credentialsSensitiveData = credentials.getSensitiveDataSerialized();
        String dataToDecrypt = credentialsSensitiveData;
        String consentId = request.getConsentId();
        // if force authenticate flag is true, we never consider cached data
        if (!request.isForceAuthenticate()) {
            // See if there is any sensitive data in the cache
            String cachedSensitiveData =
                    (String)
                            cacheClient.get(
                                    CacheScope.ENCRYPTED_CREDENTIALS_BY_CREDENTIALSID,
                                    credentials.getId());

            if (shouldPickCachedSensitiveData(cachedSensitiveData, credentialsSensitiveData)) {
                dataToDecrypt = cachedSensitiveData;
                consentId = getCachedConsentId(credentials.getId());
            }
        }

        if (!Strings.isNullOrEmpty(consentId)) {
            request.setConsentId(consentId);
        }

        if (Strings.isNullOrEmpty(dataToDecrypt)) {
            // There's nothing to decrypt. Both cache and credential were empty.
            logger.info("There is nothing to decrypt.");
            return true;
        }

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
                                CredentialsCryptoV1.DecryptedDataV1 result =
                                        CredentialsCryptoV1.decryptV1(key, v1);

                                // be aware of side-effect here! this is same credentials object
                                // as on the Request
                                credentials.addSerializedFields(result.getDecryptedFields());
                                credentials.setSensitivePayloadSerialized(
                                        result.getDecryptedPayload());

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
                            try {
                                if (v2.getFields() != null) {
                                    int keyId = v2.getFields().getKeyId();
                                    byte[] fieldsKey = cryptoWrapper.getCryptoKeyByKeyId(keyId);
                                    String result;

                                    if (charset != null) {
                                        result =
                                                CredentialsCryptoV2.decryptV2Fields(
                                                        fieldsKey, v2, charset);
                                    } else {
                                        result = CredentialsCryptoV2.decryptV2Fields(fieldsKey, v2);
                                    }
                                    // be aware of side-effect here! this is same credentials object
                                    // as on the Request
                                    credentials.addSerializedFields(result);
                                }

                                if (v2.getPayload() != null) {
                                    int keyId = v2.getPayload().getKeyId();
                                    byte[] payloadKey = cryptoWrapper.getCryptoKeyByKeyId(keyId);
                                    String result;

                                    if (charset != null) {
                                        result =
                                                CredentialsCryptoV2.decryptV2Payload(
                                                        payloadKey, v2, charset);
                                    } else {
                                        result =
                                                CredentialsCryptoV2.decryptV2Payload(
                                                        payloadKey, v2);
                                    }
                                    // be aware of side-effect here! this is same credentials object
                                    // as on the Request
                                    credentials.setSensitivePayloadSerialized(result);
                                }

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
                .handle(dataToDecrypt);
    }

    public boolean encrypt(
            CredentialsRequest request,
            boolean doUpdateCredential,
            boolean shouldCacheSensitiveData,
            Charset charset) {
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

        // Encrypt with most recent version, currently: V2
        EncryptedPayloadHead encryptedCredentials;
        if (charset != null) {
            encryptedCredentials =
                    CredentialsCryptoV2.encryptV2(
                            clusterKeyId,
                            clusterKey,
                            sensitiveInformationCredentials.getFieldsSerialized(),
                            sensitiveInformationCredentials.getSensitivePayloadSerialized(),
                            charset);
        } else {
            encryptedCredentials =
                    CredentialsCryptoV2.encryptV2(
                            clusterKeyId,
                            clusterKey,
                            sensitiveInformationCredentials.getFieldsSerialized(),
                            sensitiveInformationCredentials.getSensitivePayloadSerialized());
        }

        String serializedEncryptedCredentials =
                SerializationUtils.serializeToString(encryptedCredentials);

        // Put the `serializedEncryptedCredentials` on the credential so that it can be passed back
        // to the database
        // in createCredential() and updateCredential().
        originalCredentials.setSensitiveDataSerialized(serializedEncryptedCredentials);

        if (shouldCacheSensitiveData) {
            // Put the encrypted credentials into a temporary cache that is used by the
            // DecryptCredentialsWorkerCommand.
            // It is possible that several refresh requests for the same credential to exists on the
            // queue at the same time,
            // if the credentials are updated (such as usage of refresh token) the next request will
            // have stale data.
            // Caching the credentials with a high enough CACHE_EXPIRE_TIME allows subsequent
            // request to exchange its stale data for the updated data in the cache.
            // This is not a permanent fix and should be resolved by overhauling the credential
            // read/write logic such that a request will never have stale data in the first place.
            // This cache is read in class `CredentialsCrypto`
            cacheSensitiveData(request, encryptedCredentials, serializedEncryptedCredentials);
            // consent id is propagated back to customer cluster and used to store the sensitive
            // data on the corresponding consent
            // similarly, during bg refresh for example, we might end up using sensitive data from a
            // consent other that the one specified in the request. This means the consent has to be
            // cached along with its sensitive data
            cacheConsentId(request);
        }

        cryptoMetrics(CREDENTIALS_ENCRYPT, encryptedCredentials, true);

        if (doUpdateCredential) {
            controllerWrapper.updateCredentialSensitive(
                    request.getCredentials(),
                    serializedEncryptedCredentials,
                    request.getOperationId(),
                    request.getConsentId());
            logger.info("sensitive data saved in database");
        }

        return true;
    }

    private void cacheConsentId(CredentialsRequest request) {
        try {
            String consentId = request.getConsentId();
            if (Strings.isNullOrEmpty(consentId)) {
                return;
            }
            Future<?> future =
                    cacheClient.set(
                            CacheScope.CONSENTID_BY_CREDENTIALSID,
                            request.getCredentials().getId(),
                            CACHE_EXPIRE_TIME,
                            consentId);

            logCheckResult(future);

            logger.info("cached consentId: {} by {}", consentId, cacheClient.getClass());
        } catch (Exception e) {
            logger.error("Could not cache consent id", e);
        }
    }

    private String getCachedConsentId(String credentialsId) {
        try {
            String consentId =
                    (String) cacheClient.get(CacheScope.CONSENTID_BY_CREDENTIALSID, credentialsId);
            logger.info("using cached consentId {}", consentId);
            return consentId;
        } catch (Exception e) {
            logger.error("Could not get cached consent id", e);
            return null;
        }
    }

    private void cacheSensitiveData(
            CredentialsRequest request,
            EncryptedPayloadHead encryptedCredentials,
            String serializedEncryptedCredentials) {
        try {
            Future<?> future =
                    cacheClient.set(
                            CacheScope.ENCRYPTED_CREDENTIALS_BY_CREDENTIALSID,
                            request.getCredentials().getId(),
                            CACHE_EXPIRE_TIME,
                            serializedEncryptedCredentials);

            logCheckResult(future);

            logger.info(
                    "cached sensitive data with timestamp: {} by {}",
                    formatDate(encryptedCredentials.getTimestamp()),
                    cacheClient.getClass());
        } catch (Exception e) {
            logger.error("Could not cache sensitive data", e);
        }
    }

    private void logCheckResult(Future<?> future) {
        try {
            Object result = future.get();
            if (OperationFuture.class.isAssignableFrom(future.getClass())) {
                OperationFuture<Boolean> operationFuture = (OperationFuture<Boolean>) future;
                logger.info(
                        "Cache operation result status: {}, result: {}",
                        operationFuture.getStatus(),
                        result);
            } else {
                logger.warn(
                        "Cache operation result received class: {}, result: {}",
                        future.getClass(),
                        result);
            }
        } catch (Exception e) {
            logger.error("Error while getting result", e);
        }
    }

    private void cryptoMetrics(
            MetricId metricId, EncryptedPayloadHead encryptedCredentials, boolean success) {
        metricRegistry
                .meter(
                        metricId.label("success", success)
                                .label("version", encryptedCredentials.getVersion()))
                .inc();
    }

    private boolean shouldPickCachedSensitiveData(String cached, String incoming) {
        if (Strings.isNullOrEmpty(cached) && Strings.isNullOrEmpty(incoming)) {
            return false;
        }
        // Return the other one if one is null.
        if (Strings.isNullOrEmpty(cached)) {
            logger.info(
                    "using incoming sensitive data with timestamp: {}",
                    formatDate(getDate(incoming)));
            return false;
        } else if (Strings.isNullOrEmpty(incoming)) {
            logger.info(
                    "using cached sensitive data with timestamp: {}", formatDate(getDate(cached)));
            return true;
        }

        // Return the latest one if both are set.
        EncryptedPayloadHead cachedDeserialized =
                SerializationUtils.deserializeFromString(cached, EncryptedPayloadHead.class);

        EncryptedPayloadHead incomingDeserialized =
                SerializationUtils.deserializeFromString(incoming, EncryptedPayloadHead.class);

        if (incomingDeserialized.getTimestamp() == null
                || cachedDeserialized.getTimestamp().after(incomingDeserialized.getTimestamp())) {
            logger.info(
                    "using cached sensitive data, incoming timestamp:{}, cached timestamp:{}",
                    formatDate(incomingDeserialized.getTimestamp()),
                    formatDate(cachedDeserialized.getTimestamp()));
            return true;
        } else {
            logger.info(
                    "using incoming sensitive data, incoming timestamp:{}, cached timestamp:{}",
                    formatDate(incomingDeserialized.getTimestamp()),
                    formatDate(cachedDeserialized.getTimestamp()));

            return false;
        }
    }

    private Date getDate(String serialized) {
        if (null == serialized) {
            return null;
        }
        EncryptedPayloadHead deserialized =
                SerializationUtils.deserializeFromString(serialized, EncryptedPayloadHead.class);
        return deserialized.getTimestamp();
    }

    private String formatDate(Date date) {
        if (null == date) {
            return null;
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(date);
    }
}
