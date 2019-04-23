package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.URL;

@JsonObject
public class ProviderConfiguration {

    private String organizationId;

    private URL apiBaseURL;
    private URL pisBaseURL;
    private URL authBaseURL;
    private URL wellKnownURL;
    private ClientInfo clientInfo;

    public URL getWellKnownURL() {
        return wellKnownURL;
    }

    public URL getApiBaseURL() {
        return apiBaseURL;
    }

    public URL getPisBaseURL() {
        return pisBaseURL;
    }

    public URL getPisConsentURL() {
        return authBaseURL != null ? authBaseURL : pisBaseURL;
    }

    public URL getAuthBaseURL() {
        return authBaseURL != null ? authBaseURL : apiBaseURL;
    }

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
