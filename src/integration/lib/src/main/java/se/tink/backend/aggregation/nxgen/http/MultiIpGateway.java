package se.tink.backend.aggregation.nxgen.http;

import com.google.common.base.Strings;
import java.util.Objects;
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

    public void setMultiIpGateway(IntegrationsConfiguration integrationsConfiguration) {
        if (Objects.isNull(integrationsConfiguration)) {
            logger.warn("Proxy-setup: integrationsConfiguration is null.");
            return;
        }

        final String proxyUri = integrationsConfiguration.getProxyUri();
        if (Strings.isNullOrEmpty(proxyUri)) {
            logger.warn("Proxy-setup: proxyUri is null or empty.");
            return;
        }

        // The username (userId) and password (credentialsId) are used as a key in the proxy
        // to select the public IP address in the proxy.
        // The values themselves does not matter, as long as the same credentialsId always
        // is routed from the same public IP.
        client.setProductionProxy(proxyUri, credentials.getUserId(), credentials.getId());
        logger.info("Proxy-setup: successfully attached proxy.");
    }
}
