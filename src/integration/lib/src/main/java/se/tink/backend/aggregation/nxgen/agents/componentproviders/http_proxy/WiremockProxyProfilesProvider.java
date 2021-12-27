package se.tink.backend.aggregation.nxgen.agents.componentproviders.http_proxy;

import com.google.inject.Inject;
import se.tink.agent.runtime.operation.http.ProxyProfilesImpl;
import se.tink.agent.sdk.operation.http.ProxyProfiles;
import se.tink.backend.aggregation.nxgen.http.proxy.NoopProxyProfile;

public class WiremockProxyProfilesProvider implements ProxyProfilesProvider {
    private final ProxyProfilesImpl proxyProfiles;

    @Inject
    public WiremockProxyProfilesProvider() {
        // Wiremock should not use proxies, therefore instantiate using `NoopProxyProfile`.
        this.proxyProfiles = new ProxyProfilesImpl(new NoopProxyProfile(), new NoopProxyProfile());
    }

    @Override
    public ProxyProfiles getProxyProfiles() {
        return proxyProfiles;
    }
}
