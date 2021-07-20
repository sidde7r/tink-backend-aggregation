package se.tink.backend.aggregation.nxgen.agents.proxy;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.PasswordBasedProxyConfiguration;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

@Slf4j
public final class ProxyConfigurator {

    private static final String PROXY = "Proxy";
    private final AgentsServiceConfiguration agentsServiceConfiguration;

    @Inject
    public ProxyConfigurator(AgentsServiceConfiguration agentsServiceConfiguration) {
        this.agentsServiceConfiguration = agentsServiceConfiguration;
    }

    public TinkHttpClient addProxy(TinkHttpClient client) {
        try {
            String countryCode = client.getProvider().getMarket().toLowerCase();
            String marketProxy = countryCode + PROXY;

            if (this.agentsServiceConfiguration.isFeatureEnabled(marketProxy)) {
                // Setting proxy via TPP
                final PasswordBasedProxyConfiguration proxyConfiguration =
                        this.agentsServiceConfiguration.getCountryProxy(countryCode);

                if (isNotEmptyProxyConfiguration(proxyConfiguration)) {
                    log.info(
                            "[PROXY] Setting proxy {} for market {} with username {}",
                            proxyConfiguration.getHost(),
                            countryCode.toUpperCase(),
                            proxyConfiguration.getUsername());

                    client.setProductionProxy(
                            proxyConfiguration.getHost(),
                            proxyConfiguration.getUsername(),
                            proxyConfiguration.getPassword());
                } else {
                    log.warn(
                            "[PROXY] Configuration proxy for {} market is unavailable",
                            countryCode.toUpperCase());
                }
            }
        } catch (RuntimeException e) {
            log.error(
                    "[PROXY] Setting proxy for {} finish with fail due to {}",
                    client.getProvider().getName(),
                    e.getMessage());
        }
        return client;
    }

    private boolean isNotEmptyProxyConfiguration(
            PasswordBasedProxyConfiguration proxyConfiguration) {
        return StringUtils.isNotEmpty(proxyConfiguration.getUsername())
                && StringUtils.isNotEmpty(proxyConfiguration.getHost())
                && StringUtils.isNotEmpty(proxyConfiguration.getPassword());
    }
}
