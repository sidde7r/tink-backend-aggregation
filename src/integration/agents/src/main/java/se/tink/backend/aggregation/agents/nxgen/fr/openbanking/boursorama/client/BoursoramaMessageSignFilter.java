package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class BoursoramaMessageSignFilter extends Filter {

    private final BoursoramaSignatureHeaderGenerator boursoramaSignatureHeaderGenerator;

    public BoursoramaMessageSignFilter(
            BoursoramaSignatureHeaderGenerator boursoramaSignatureHeaderGenerator) {
        this.boursoramaSignatureHeaderGenerator = boursoramaSignatureHeaderGenerator;
    }

    @Override
    public HttpResponse handle(HttpRequest request)
            throws HttpClientException, HttpResponseException {

        appendAdditionalHeaders(request);
        String serializedBody = prepareRequestBody(request);
        addDigestHeader(serializedBody, request);
        addSignatureHeader(request);
        return nextFilter(request);
    }


    private void appendAdditionalHeaders(HttpRequest request) {
        request.getHeaders().add(Psd2Headers.Keys.X_REQUEST_ID, UUID.randomUUID());
//        String date =
//            DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneId.of("GMT")));
//        request.getHeaders().add(Psd2Headers.Keys.DATE, date);
    }

    private void addSignatureHeader(HttpRequest request) {
        String signatureHeaderValue =
                boursoramaSignatureHeaderGenerator.getSignatureHeaderValue(
                        request.getMethod().name(),
                        request.getURI(),
                        (String) request.getHeaders().getFirst(Psd2Headers.Keys.DIGEST),
                        String.valueOf(
                                request.getHeaders().getFirst(Psd2Headers.Keys.X_REQUEST_ID)),
                        (String) request.getHeaders().getFirst(Psd2Headers.Keys.DATE),
                        String.valueOf(request.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE)));

        request.getHeaders().add(Psd2Headers.Keys.SIGNATURE, signatureHeaderValue);
    }

    private void addDigestHeader(String serializedBody, HttpRequest request) {
        request.getHeaders()
                .add(
                        Psd2Headers.Keys.DIGEST,
                        boursoramaSignatureHeaderGenerator.getDigestHeaderValue(serializedBody));
    }

    private String prepareRequestBody(HttpRequest request) {
        if (request.getBody() == null) {
            return "";
        } else {
            String serializedBody = serializeBodyIfNecessary(request);
            request.setBody(serializedBody);
            return serializedBody;
        }
    }

    private String serializeBodyIfNecessary(HttpRequest request) {
        Object requestBody = request.getBody();
        return requestBody instanceof String ? (String) requestBody : serialize(requestBody);
    }

    private String serialize(Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }


}
