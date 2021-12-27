package se.tink.backend.aggregation.nxgen.agents.componentproviders.http_proxy;

import se.tink.agent.sdk.operation.http.ProxyProfiles;

public interface ProxyProfilesProvider {
    ProxyProfiles getProxyProfiles();
}
