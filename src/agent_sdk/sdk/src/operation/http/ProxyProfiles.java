package se.tink.agent.sdk.operation.http;

import se.tink.backend.aggregation.nxgen.http.proxy.ProxyProfile;

public interface ProxyProfiles {
    /** @return A proxy profile configured to, if possible, egress via an AWS IP range. */
    ProxyProfile getAwsProxyProfile();

    /**
     * Utilize the BrightData (formerly known as Luminati) proxy network to egress from an IP
     * address in the provider's market. If the current provider's market has not been configured
     * then this proxy profile will not configure the proxy.
     *
     * @return A proxy profile configured to, if possible, egress via an IP address in the
     *     provider's market.
     */
    ProxyProfile getMarketProxyProfile();
}
