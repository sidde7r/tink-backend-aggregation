package se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.NorwegianConstants;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.NorwegianConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterOrder;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterPhases;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.header.SignatureHeaderGenerator;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@FilterOrder(category = FilterPhases.REQUEST_HANDLE, order = Integer.MAX_VALUE)
public class NorwegianSigningFilter extends Filter {

    private static final String WHITESPACE = " ";
    private static final String REQUEST_TARGET = "(request-target)";

    private final SignatureHeaderGenerator signatureHeaderGenerator;

    public NorwegianSigningFilter(String qsealcThumbprint, QsealcSigner qsealcSigner) {
        this.signatureHeaderGenerator =
                new SignatureHeaderGenerator(
                        NorwegianConstants.SIGNATURE_FORMAT,
                        NorwegianConstants.SIGNABLE_HEADERS,
                        qsealcThumbprint,
                        qsealcSigner);
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        addHostHeader(httpRequest);
        addDigestHeader(httpRequest);
        sign(httpRequest);
        return nextFilter(httpRequest);
    }

    private void addHostHeader(HttpRequest httpRequest) {
        httpRequest.getHeaders().putSingle(HeaderKeys.HOST, httpRequest.getURI().getHost());
    }

    private void sign(HttpRequest httpRequest) {

        final Map<String, Object> flatHeaders = new LinkedHashMap<>();
        flatHeaders.put(REQUEST_TARGET, buildRequestTargetHeader(httpRequest));
        httpRequest.getHeaders().entrySet().stream()
                .filter(entry -> entry.getValue().size() >= 1)
                .forEach(a -> flatHeaders.put(a.getKey(), a.getValue().get(0)));
        httpRequest
                .getHeaders()
                .putSingle(
                        HeaderKeys.SIGNATURE,
                        signatureHeaderGenerator.generateSignatureHeader(flatHeaders));
    }

    private String buildRequestTargetHeader(HttpRequest request) {
        StringBuilder builder =
                new StringBuilder()
                        .append(request.getMethod().name().toLowerCase())
                        .append(WHITESPACE)
                        .append(request.getURI().getPath());
        if (request.getURI().getQuery() != null) {
            builder.append("?").append(request.getURI().getQuery());
        }
        return builder.toString();
    }

    private void addDigestHeader(HttpRequest httpRequest) {
        if (httpRequest.getBody() != null) {
            String body = getSerializedBody(httpRequest);
            // this is to ensure the serialized body is binary the same as the one
            // used for signature
            httpRequest.setBody(body);
            String digest = EncodingUtils.encodeAsBase64String(Hash.sha256(body));
            httpRequest.getHeaders().putSingle(HeaderKeys.DIGEST, "SHA-256=" + digest);
        }
    }

    private String getSerializedBody(HttpRequest request) {
        if (request.getBody() instanceof String) { // already serialized
            return (String) request.getBody();
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(request.getBody());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    "Unable to perform signing, cannot serialize request body", e);
        }
    }
}
