package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.configuration;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProviderConfiguration {

    private ClientInfo clientInfo;

    public ProviderConfiguration() {}

    public ProviderConfiguration(ClientInfo clientInfo) {
        this.clientInfo = clientInfo;
    }

    public void validate() {
        Preconditions.checkNotNull(clientInfo);
    }

    public ClientInfo getClientInfo() {
        return clientInfo;
    }
}
