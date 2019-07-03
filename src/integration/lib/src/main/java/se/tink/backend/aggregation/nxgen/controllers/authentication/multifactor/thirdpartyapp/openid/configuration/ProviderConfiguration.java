package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProviderConfiguration {

    private String organizationId;

    private ClientInfo clientInfo;

    public void validate() {
        Preconditions.checkNotNull(clientInfo);
    }

    public ClientInfo getClientInfo() {
        return clientInfo;
    }

    public String getOrganizationId() {
        return organizationId;
    }
}
