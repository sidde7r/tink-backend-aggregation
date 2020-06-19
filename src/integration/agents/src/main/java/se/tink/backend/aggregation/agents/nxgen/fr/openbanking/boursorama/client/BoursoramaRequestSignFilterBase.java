package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client;

import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.BoursoramaConstants.ZONE_ID;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.serialization.utils.SerializationUtils;

public abstract class BoursoramaRequestSignFilterBase extends Filter {

    @Override
    public HttpResponse handle(HttpRequest request)
            throws HttpClientException, HttpResponseException {

        if (request.getMethod() != getHttpMethod()) {
            return nextFilter(request);
        }

        appendAdditionalHeaders(request);
        addDigestHeader(prepareRequestBody(request), request);
        addSignatureHeader(request);
        return nextFilter(request);
    }

    abstract BoursoramaSignatureHeaderGenerator getBoursoramaSignatureHeaderGenerator();

    abstract String getSignatureHeaderValue(HttpRequest request);

    abstract HttpMethod getHttpMethod();

    abstract String prepareRequestBody(HttpRequest request);

    String serializeBodyIfNecessary(HttpRequest request) {
        Object requestBody = request.getBody();
        return requestBody instanceof String
                ? (String) requestBody
                : SerializationUtils.serializeToString(requestBody);
    }

    private void appendAdditionalHeaders(HttpRequest request) {
        request.getHeaders().add(Psd2Headers.Keys.X_REQUEST_ID, UUID.randomUUID());

        final String date = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZONE_ID));
        request.getHeaders().add(Psd2Headers.Keys.DATE, date);
    }

    private void addDigestHeader(String serializedBody, HttpRequest request) {
        request.getHeaders()
                .add(
                        Psd2Headers.Keys.DIGEST,
                        getBoursoramaSignatureHeaderGenerator()
                                .getDigestHeaderValue(serializedBody));
    }

    private void addSignatureHeader(HttpRequest request) {
        request.getHeaders().add(Psd2Headers.Keys.SIGNATURE, getSignatureHeaderValue(request));
    }
}
