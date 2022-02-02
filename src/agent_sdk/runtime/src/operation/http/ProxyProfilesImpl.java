package src.agent_sdk.runtime.src.operation.http;

import se.tink.agent.sdk.operation.http.ProxyProfiles;
import se.tink.backend.aggregation.nxgen.http.proxy.ProxyProfile;

public class ProxyProfilesImpl implements ProxyProfiles {
    private final ProxyProfile awsProxyProfile;
    private final ProxyProfile marketProxyProfile;

    public ProxyProfilesImpl(ProxyProfile awsProxyProfile, ProxyProfile marketProxyProfile) {
        this.awsProxyProfile = awsProxyProfile;
        this.marketProxyProfile = marketProxyProfile;
    }

    @Override
    public ProxyProfile getAwsProxyProfile() {
        return this.awsProxyProfile;
    }

    @Override
    public ProxyProfile getMarketProxyProfile() {
        return this.marketProxyProfile;
    }
}
