package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid;

import com.google.common.base.Strings;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import javax.ws.rs.core.MultivaluedMap;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class OpenIdAuthenticatedHttpFilter extends Filter {
    private final OAuth2Token accessToken;
    private final RandomValueGenerator randomValueGenerator;

    public OpenIdAuthenticatedHttpFilter(
            OAuth2Token accessToken, RandomValueGenerator randomValueGenerator) {
        this.accessToken = accessToken;
        this.randomValueGenerator = randomValueGenerator;
    }

    private boolean verifyInteractionId(String interactionId, HttpResponse httpResponse) {

        MultivaluedMap<String, String> headers = httpResponse.getHeaders();
        if (Objects.isNull(headers)) {
            return false;
        }

        Optional<String> receivedInteractionId =
                headers.keySet().stream()
                        .filter(
                                key ->
                                        key.toLowerCase()
                                                .equals(
                                                        OpenIdConstants.HttpHeaders
                                                                .X_FAPI_INTERACTION_ID
                                                                .toLowerCase()))
                        .map(key -> headers.getFirst(key))
                        .findFirst();

        if (!receivedInteractionId.isPresent()
                || Strings.isNullOrEmpty(receivedInteractionId.get())) {
            return false;
        }

        // Some banks have a bug where they send our interaction Id twice, comma-separated.
        return Arrays.stream(receivedInteractionId.get().split(","))
                .anyMatch(interactionId::equals);
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {

        String interactionId = randomValueGenerator.getUUID().toString();

        MultivaluedMap<String, Object> headers = httpRequest.getHeaders();
        headers.add(OpenIdConstants.HttpHeaders.AUTHORIZATION, accessToken.toAuthorizeHeader());
        // Setting these 2 headers is optional according to the OpenID and OpenBanking specs.
        // If we set the timestamp then the actually accepted formats don't follow the
        // specifications.
        // We don't have the client IP.
        // Decided to not set these headers until they're actually required.
        // headers.add(OpenIdConstants.HttpHeaders.X_FAPI_CUSTOMER_LAST_LOGGED_TIME,
        // customerLastLoggedInTime);
        // headers.add(OpenIdConstants.HttpHeaders.X_FAPI_CUSTOMER_IP_ADDRESS, customerIp);
        headers.add(OpenIdConstants.HttpHeaders.X_FAPI_INTERACTION_ID, interactionId);

        HttpResponse httpResponse = nextFilter(httpRequest);

        // Only validate for non-error responses.
        if (httpResponse.getStatus() < 400) {
            if (!verifyInteractionId(interactionId, httpResponse)) {
                throw new HttpResponseException(
                        "X_FAPI_INTERACTION_ID does not match.", httpRequest, httpResponse);
            }
        }

        return httpResponse;
    }
}
