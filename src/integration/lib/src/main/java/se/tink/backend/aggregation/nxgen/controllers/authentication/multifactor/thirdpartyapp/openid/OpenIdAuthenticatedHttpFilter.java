package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid;

import com.google.common.base.Strings;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import javax.ws.rs.core.MultivaluedMap;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ProviderConfiguration;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;

public class OpenIdAuthenticatedHttpFilter extends Filter {
    private final OAuth2Token accessToken;
    private final ProviderConfiguration providerConfiguration;
    private final String customerIp;
    private final String customerLastLoggedInTime;

    public OpenIdAuthenticatedHttpFilter(
            OAuth2Token accessToken,
            ProviderConfiguration providerConfiguration,
            String customerIp,
            String customerLastLoggedInTime) {
        this.accessToken = accessToken;
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

        // Some banks have a bug where they send our interaction Id twice, comma-separated.
        return Arrays.stream(receivedInteractionId.split(","))
                .anyMatch(interactionId::equals);
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest) throws HttpClientException, HttpResponseException {

        String interactionId = generateInteractionId();

        MultivaluedMap<String, Object> headers = httpRequest.getHeaders();
        headers.add(OpenIdConstants.HttpHeaders.AUTHORIZATION, accessToken.toAuthorizeHeader());
        headers.add(OpenIdConstants.HttpHeaders.X_FAPI_FINANCIAL_ID, providerConfiguration.getOrganizationId());
        // Setting these 2 headers is optional according to the OpenID and OpenBanking specs.
        // If we set the timestamp then the actually accepted formats don't follow the specifications.
        // We don't have the client IP.
        // Decided to not set these headers until they're actually required.
        // headers.add(OpenIdConstants.HttpHeaders.X_FAPI_CUSTOMER_LAST_LOGGED_TIME, customerLastLoggedInTime);
        // headers.add(OpenIdConstants.HttpHeaders.X_FAPI_CUSTOMER_IP_ADDRESS, customerIp);
        headers.add(OpenIdConstants.HttpHeaders.X_FAPI_INTERACTION_ID, interactionId);

        HttpResponse httpResponse = nextFilter(httpRequest);

        if (!verifyInteractionId(interactionId, httpResponse)) {
            throw new HttpResponseException(String.format("%d: %s", httpResponse.getStatus(), httpResponse.getBody(String.class)) , httpRequest, httpResponse);
        }

        return httpResponse;
    }
}
