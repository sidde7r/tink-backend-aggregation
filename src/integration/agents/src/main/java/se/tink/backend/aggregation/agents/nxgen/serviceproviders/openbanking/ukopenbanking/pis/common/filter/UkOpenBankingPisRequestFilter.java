package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.filter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.ws.rs.core.MultivaluedMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature.UkOpenBankingJwtSignatureHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.storage.UkOpenBankingPaymentStorage;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RequiredArgsConstructor
@Slf4j
public class UkOpenBankingPisRequestFilter extends Filter {

    private static final String X_IDEMPOTENCY_KEY_HEADER = "x-idempotency-key";
    private static final String X_JWS_SIGNATURE_HEADER = "x-jws-signature";

    private final UkOpenBankingJwtSignatureHelper jwtSignatureHelper;

    @Getter private final UkOpenBankingPaymentStorage storage;
    private final RandomValueGenerator randomValueGenerator;

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        addHeaders(httpRequest);

        final HttpResponse httpResponse = nextFilter(httpRequest);

        validateResponse(httpResponse, httpRequest);
        return httpResponse;
    }

    protected void validateResponse(HttpResponse httpResponse, HttpRequest httpRequest) {
        final MultivaluedMap<String, Object> headers = httpRequest.getHeaders();
        final String xFapiInteractionId =
                headers.getFirst(OpenIdConstants.HttpHeaders.X_FAPI_INTERACTION_ID).toString();

        if (httpResponse.getStatus() < 400
                && !isInteractionIdValidInResponse(xFapiInteractionId, httpResponse)) {
            throw new HttpResponseException(
                    "X_FAPI_INTERACTION_ID does not match.", httpRequest, httpResponse);
        }
    }

    public void setSoftwareId(String softwareId) {
        Objects.requireNonNull(softwareId);
        jwtSignatureHelper.setSoftwareId(softwareId);
    }

    private void addHeaders(HttpRequest httpRequest) {
        final MultivaluedMap<String, Object> headers = httpRequest.getHeaders();
        addAuthorizationHeader(headers);
        addInteractionIdHeader(headers);
        addXIdempotencyKeyHeader(headers);
        addSignatureHeaderIfBodyIsPresent(httpRequest, headers);
    }

    private void addAuthorizationHeader(MultivaluedMap<String, Object> headers) {
        final OAuth2Token accessToken = storage.getToken();
        headers.add(OpenIdConstants.HttpHeaders.AUTHORIZATION, accessToken.toAuthorizeHeader());
    }

    private void addInteractionIdHeader(MultivaluedMap<String, Object> headers) {
        final String interactionId = randomValueGenerator.getUUID().toString();
        headers.add(OpenIdConstants.HttpHeaders.X_FAPI_INTERACTION_ID, interactionId);
    }

    private void addXIdempotencyKeyHeader(MultivaluedMap<String, Object> headers) {
        headers.add(X_IDEMPOTENCY_KEY_HEADER, randomValueGenerator.generateRandomHexEncoded(8));
    }

    private void addSignatureHeaderIfBodyIsPresent(
            HttpRequest httpRequest, MultivaluedMap<String, Object> headers) {
        if (Optional.ofNullable(httpRequest.getBody()).isPresent()) {
            log.info(
                    "templog jwt token value="
                            + Optional.ofNullable(httpRequest.getBody())
                                    .map(jwtSignatureHelper::createJwtSignature)
                                    .get());
        } else {
            log.info("httpRequest.getBody() is empty");
        }

        Optional.ofNullable(httpRequest.getBody())
                .map(jwtSignatureHelper::createJwtSignature)
                .ifPresent(signature -> headers.add(X_JWS_SIGNATURE_HEADER, signature));
    }

    private static boolean isInteractionIdValidInResponse(
            String interactionId, HttpResponse httpResponse) {

        return Optional.ofNullable(httpResponse.getHeaders())
                .map(Map::entrySet)
                .flatMap(
                        entries ->
                                entries.stream()
                                        .filter(
                                                entry ->
                                                        entry.getKey()
                                                                .equalsIgnoreCase(
                                                                        OpenIdConstants.HttpHeaders
                                                                                .X_FAPI_INTERACTION_ID))
                                        .map(Map.Entry::getValue)
                                        .flatMap(Collection::stream)
                                        .filter(StringUtils::isNotBlank)
                                        .map(id -> id.split(","))
                                        .flatMap(Arrays::stream)
                                        .filter(interactionId::equals)
                                        .findAny())
                .isPresent();
    }
}
