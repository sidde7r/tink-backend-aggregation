package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail;

import static java.util.Objects.requireNonNull;
import static se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.DateConfig.ZONE_ID;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.header.SignatureHeaderGenerator;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class ChebancaRequestBuilder {

    private final TinkHttpClient client;
    private SignatureHeaderGenerator signatureHeaderGenerator;

    public ChebancaRequestBuilder(
            TinkHttpClient client, SignatureHeaderGenerator signatureHeaderGenerator) {
        this.client = requireNonNull(client);
        this.signatureHeaderGenerator = requireNonNull(signatureHeaderGenerator);
    }

    public RequestBuilder buildRequest(URL url, String requestBody, String httpMethod) {
        String date = RequestDateFormatter.getDateFormatted(ZonedDateTime.now(ZONE_ID));
        String requestId = UUID.randomUUID().toString();
        RequestBuilder requestBuilder = client.request(url);
        String digest = null;
        if (requestBody != null) {
            digest = DigestGenerator.generateDigest(requestBody);
            requestBuilder.header(ChebancaConstants.HeaderKeys.DIGEST, digest);
        }
        Map<String, Object> headers = getSigningHeaders(requestId, digest, date, httpMethod, url);

        return build(requestBuilder, date, requestId, headers);
    }

    private RequestBuilder build(
            RequestBuilder requestBuilder,
            String date,
            String requestId,
            Map<String, Object> headers) {
        return requestBuilder
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(ChebancaConstants.HeaderKeys.TPP_REQUEST_ID, requestId)
                .header(ChebancaConstants.HeaderKeys.DATE, date)
                .header(
                        ChebancaConstants.HeaderKeys.SIGNATURE,
                        signatureHeaderGenerator.generateSignatureHeader(headers));
    }

    private Map<String, Object> getSigningHeaders(
            String requestId, String digest, String date, String httpMethod, URL url) {
        Map<String, Object> headers = new HashMap<>();
        headers.put(
                ChebancaConstants.QueryKeys.REQUEST_TARGET,
                URLFormatter.formatToString(httpMethod, url));
        Optional.ofNullable(digest)
                .ifPresent(d -> headers.put(ChebancaConstants.HeaderKeys.DIGEST, d));
        headers.put(ChebancaConstants.HeaderKeys.DATE, date);
        headers.put(ChebancaConstants.HeaderKeys.TPP_REQUEST_ID, requestId);

        return headers;
    }
}
