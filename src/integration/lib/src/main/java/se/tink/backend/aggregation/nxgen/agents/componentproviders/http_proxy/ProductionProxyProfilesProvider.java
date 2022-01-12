package se.tink.backend.aggregation.nxgen.agents.componentproviders.http_proxy;

import com.google.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.agent.runtime.operation.http.ProxyProfilesImpl;
import se.tink.agent.sdk.operation.http.ProxyProfiles;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.configuration.IntegrationsConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.http.proxy.AuthenticatedProxyProfile;
import se.tink.backend.aggregation.nxgen.http.proxy.NoopProxyProfile;
import se.tink.backend.aggregation.nxgen.http.proxy.ProxyProfile;
import se.tink.libraries.credentials.service.CredentialsRequest;

@Slf4j
public class ProductionProxyProfilesProvider implements ProxyProfilesProvider {
    private static final String FEATURE_FLAG_SUFFIX = "Proxy";

    private final ProxyProfilesImpl proxyProfiles;

    @Inject
    public ProductionProxyProfilesProvider(
            AgentsServiceConfiguration agentsServiceConfiguration,
            CredentialsRequest credentialsRequest) {
        ProxyProfile awsProxyProfile =
                tryConstructAwsProxyProfile(agentsServiceConfiguration, credentialsRequest)
                        .orElseGet(NoopProxyProfile::new);
        ProxyProfile marketProxyProfile =
                tryConstructMarketProxyProfile(agentsServiceConfiguration, credentialsRequest)
                        .orElseGet(NoopProxyProfile::new);
        this.proxyProfiles = new ProxyProfilesImpl(awsProxyProfile, marketProxyProfile);
    }

    @Override
    public ProxyProfiles getProxyProfiles() {
        return proxyProfiles;
    }

    private Optional<ProxyProfile> tryConstructAwsProxyProfile(
            AgentsServiceConfiguration agentsServiceConfiguration,
            CredentialsRequest credentialsRequest) {
        IntegrationsConfiguration integrationsConfiguration =
                agentsServiceConfiguration.getIntegrations();
        if (Objects.isNull(integrationsConfiguration)) {
            log.warn("[PROXY] integrationsConfiguration is null.");
            return Optional.empty();
        }

        final List<String> proxyHosts = integrationsConfiguration.getProxyUris();
        if (Objects.isNull(proxyHosts) || proxyHosts.isEmpty()) {
            return Optional.empty();
        }

        String userId = credentialsRequest.getCredentials().getUserId();
        String credentialsId = credentialsRequest.getCredentials().getId();
        String proxyHost = pickAwsProxyHostBasedOnUserId(proxyHosts, userId);

        return Optional.of(new AuthenticatedProxyProfile(proxyHost, userId, credentialsId));
    }

    private String pickAwsProxyHostBasedOnUserId(
            final List<String> proxyHosts, final String userId) {
        final int len = proxyHosts.size();
        final int index = ((userId.hashCode() % len) + len) % len;
        return proxyHosts.get(index);
    }

    private Optional<ProxyProfile> tryConstructMarketProxyProfile(
            AgentsServiceConfiguration agentsServiceConfiguration,
            CredentialsRequest credentialsRequest) {
        try {
            String userId = credentialsRequest.getCredentials().getUserId();
            Provider provider = credentialsRequest.getProvider();
            String countryCode = provider.getMarket().toLowerCase();

            String featureFlag = countryCode + FEATURE_FLAG_SUFFIX;
            if (!agentsServiceConfiguration.isFeatureEnabled(featureFlag)) {
                return Optional.empty();
            }

            return agentsServiceConfiguration.getCountryProxyProfile(
                    countryCode, userId.hashCode());
        } catch (RuntimeException e) {
            log.error("[PROXY] Constructing MarketProxyProfile failed.", e);
            return Optional.empty();
        }
    }
}
