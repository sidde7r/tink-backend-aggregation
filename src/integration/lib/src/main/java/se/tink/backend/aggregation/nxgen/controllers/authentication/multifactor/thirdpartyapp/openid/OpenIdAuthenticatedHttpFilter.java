package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid;

import com.google.common.base.Strings;
import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.Optional;
import javax.ws.rs.core.MultivaluedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class OpenIdAuthenticatedHttpFilter extends Filter {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
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
                                        key.equalsIgnoreCase(
                                                OpenIdConstants.HttpHeaders.X_FAPI_INTERACTION_ID))
                        .map(headers::getFirst)
                        .findFirst();

        if (!receivedInteractionId.isPresent()
                || Strings.isNullOrEmpty(receivedInteractionId.get())) {
            return false;
        }
        logger.info("interaction id {} from response header", receivedInteractionId.get());
        return receivedInteractionId.get().contains(interactionId);
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {

        String interactionId = randomValueGenerator.getUUID().toString();
        logger.info("assigning interaction id {} with header", interactionId);
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
        if (httpResponse.getStatus() < 400 && !verifyInteractionId(interactionId, httpResponse)) {
            throw new HttpResponseException(
                    "X_FAPI_INTERACTION_ID does not match.", httpRequest, httpResponse);
        }

        return httpResponse;
    }
}
