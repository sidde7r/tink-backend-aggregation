package se.tink.backend.integration.tpp_secrets_service.client;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.integration.tpp_secrets_service.client.entities.SecretsEntityCore;
import se.tink.backend.secretservice.grpc.GetAllSecretsResponse;
import se.tink.backend.secretservice.grpc.GetSecretsRequest;
import se.tink.backend.secretservice.grpc.InternalSecretsServiceGrpc;
import se.tink.backend.secretservice.grpc.TppSecret;

class AllSecretsFetcher {

    private static final Logger log = LoggerFactory.getLogger(AllSecretsFetcher.class);

    private final boolean enabled;
    private InternalSecretsServiceGrpc.InternalSecretsServiceBlockingStub
            internalSecretsServiceStub;

    public AllSecretsFetcher(
            boolean enabled,
            InternalSecretsServiceGrpc.InternalSecretsServiceBlockingStub
                    internalSecretsServiceStub) {
        this.enabled = enabled;
        this.internalSecretsServiceStub = internalSecretsServiceStub;
    }

    public Optional<SecretsEntityCore> getAllSecrets(
            String appId, String clusterId, String certId, String providerId) {
        log.info(
                "calling SecretService getAllSecrets with params: appId:{}, clusterId:{}, certId: {}, providerId:{}",
                appId,
                clusterId,
                certId,
                providerId);
        if (!enabled) {
            log.warn(
                    "Trying to call getAllSecrets for an instance of TppSecretsServiceClientImpl when the configuration says it is not enabled.");
            return Optional.empty();
        }

        // TODO: Remove this once Access team confirms there are no null appIds
        if (StringUtils.isBlank(appId) || StringUtils.isBlank(providerId)) {
            return Optional.empty();
        }

        GetSecretsRequest getSecretsRequest = buildRequest(appId, clusterId, certId, providerId);

        GetAllSecretsResponse response =
                internalSecretsServiceStub.getAllSecrets(getSecretsRequest);

        List<TppSecret> allSecretsList = new ArrayList<>();
        allSecretsList.addAll(response.getEncryptedSecretsList());
        allSecretsList.addAll(response.getSecretsList());

        return Optional.of(
                new SecretsEntityCore.Builder()
                        .setSecrets(
                                allSecretsList.stream()
                                        .collect(
                                                Collectors.toMap(
                                                        TppSecret::getKey, TppSecret::getValue)))
                        .setRedirectUrls(response.getRedirectUrlsList())
                        .setScopes(response.getScopesList())
                        .setQwac(response.getCertificates().getQwac())
                        .setQsealc(response.getCertificates().getQsealc())
                        .build());
    }

    private GetSecretsRequest buildRequest(
            String appId, String clusterId, String certId, String providerId) {
        Preconditions.checkNotNull(appId, "appId must not be null");
        Preconditions.checkNotNull(clusterId, "clusterId must not be null");
        Preconditions.checkNotNull(providerId, "providerId must not be null");

        return GetSecretsRequest.newBuilder()
                .setProviderId(providerId)
                .setAppId(appId)
                .setClusterId(clusterId)
                .setCertId(Strings.isNullOrEmpty(certId) ? "" : certId)
                .build();
    }
}
