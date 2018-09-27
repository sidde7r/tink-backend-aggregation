package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants;
import se.tink.backend.aggregation.nxgen.http.URL;

@JsonObject
public class ProviderConfiguration {

    private String organizationId;
    private URL apiBaseURL;
    private URL wellKnownURL;
    private ClientInfo clientInfo;

    public URL getWellKnownURL() {
        return wellKnownURL;
    }

    public URL getAccountRequestsURL() {
        return apiBaseURL.concat(OpenIdConstants.ApiServices.ACCOUNT_REQUESTS);
    }

    public URL getPaymentsURL() {
        return apiBaseURL.concat(OpenIdConstants.ApiServices.PAYMENTS);
    }

    public URL getPaymentSubmissionsURL(String paymentSubmissionId) {
        return apiBaseURL
                .concat(OpenIdConstants.ApiServices.PAYMENT_SUBMISSIONS + paymentSubmissionId);
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
