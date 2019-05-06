package se.tink.backend.aggregation.nxgen.http;

import com.google.common.base.Strings;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.configuration.IntegrationsConfiguration;

public final class MultiIpGateway {
    private final Logger logger = LoggerFactory.getLogger(MultiIpGateway.class);
    private final TinkHttpClient client;
    private final Credentials credentials;

    public MultiIpGateway(final TinkHttpClient client, final Credentials credentials) {
        this.client = client;
        this.credentials = credentials;
    }

    private static String toProxy(final List<String> proxyUris, final String userId) {
        if (proxyUris.size() == 0) {
            return null;
        }
        final int len = proxyUris.size();
        final int index = ((userId.hashCode() % len) + len) % len;
        return proxyUris.get(index);
    }

    public void setMultiIpGateway(final IntegrationsConfiguration integrationsConfiguration) {
        if (Objects.isNull(integrationsConfiguration)) {
            logger.warn("Proxy-setup: integrationsConfiguration is null.");
            return;
        }

        final List<String> proxyUris = integrationsConfiguration.getProxyUris();

        final String proxyUri =
                Optional.ofNullable(proxyUris)
                        .map(uris -> toProxy(uris, credentials.getUserId()))
                        .orElseGet(
                                () -> {
                                    logger.warn("proxyUris is null -- falling back to proxyUri");
                                    return integrationsConfiguration.getProxyUri();
                                });

        if (Strings.isNullOrEmpty(proxyUri)) {
            logger.warn("Proxy-setup: proxyUri is null or empty.");
            return;
        }

        // The username (userId) and password (credentialsId) are used as a key in the proxy
        // to select the public IP address in the proxy.
        // The values themselves does not matter, as long as the same credentialsId always
        // is routed from the same public IP.
        client.setProductionProxy(proxyUri, credentials.getUserId(), credentials.getId());
        logger.info("Proxy-setup: successfully attached to {}", proxyUri);
    }
}
