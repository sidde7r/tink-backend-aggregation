package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid;

import com.google.common.base.Strings;
import java.util.Objects;
import java.util.UUID;
import javax.ws.rs.core.MultivaluedMap;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ProviderConfiguration;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;

public class OpenIdAuthenticatedHttpFilter extends Filter {
    private final AuthenticationToken authToken;
    private final ProviderConfiguration providerConfiguration;
    private final String customerIp;
    private final String customerLastLoggedInTime;

    public OpenIdAuthenticatedHttpFilter(
            AuthenticationToken authToken,
            ProviderConfiguration providerConfiguration,
            String customerIp,
            String customerLastLoggedInTime) {
        this.authToken = authToken;
        this.providerConfiguration = providerConfiguration;
        this.customerIp = customerIp;
        this.customerLastLoggedInTime = customerLastLoggedInTime;
    }

    private static String generateInteractionId() {
        return UUID.randomUUID().toString();
    }

    private boolean verifyInteractionId(String interactionId, HttpResponse httpResponse) {

        MultivaluedMap<String, String> headers = httpResponse.getHeaders();
        if (Objects.isNull(headers)) {
            return false;
        }

        String receivedInteractionId = headers.getFirst(OpenIdConstants.HttpHeaders.X_FAPI_INTERACTION_ID);
        if (Strings.isNullOrEmpty(receivedInteractionId)) {
            return false;
        }

        return interactionId.equals(receivedInteractionId);
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest) throws HttpClientException, HttpResponseException {

        String interactionId = generateInteractionId();

        MultivaluedMap<String, Object> headers = httpRequest.getHeaders();
        headers.add(OpenIdConstants.HttpHeaders.AUTHORIZATION, authToken.toAuthorizeHeader());
        headers.add(OpenIdConstants.HttpHeaders.X_FAPI_FINANCIAL_ID, providerConfiguration.getOrganizationId());
        headers.add(OpenIdConstants.HttpHeaders.X_FAPI_CUSTOMER_LAST_LOGGED_TIME, customerLastLoggedInTime);
        headers.add(OpenIdConstants.HttpHeaders.X_FAPI_CUSTOMER_IP_ADDRESS, customerIp);
        headers.add(OpenIdConstants.HttpHeaders.X_FAPI_INTERACTION_ID, interactionId);

        HttpResponse httpResponse = nextFilter(httpRequest);

        if (!verifyInteractionId(interactionId, httpResponse)) {
            throw new HttpResponseException(httpRequest, httpResponse);
        }

        return httpResponse;
    }
}
