package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.apiclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.IspConstants.HeaderKeys;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterOrder;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterPhases;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@FilterOrder(category = FilterPhases.SEND, order = 0)
public class IspSignEncryptFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        boolean shouldEncrypt = shouldEncrypt(httpRequest);
        if (shouldEncrypt) {
            signAndEncryptRequest(httpRequest);
            HttpResponse response = nextFilter(httpRequest);
            if (response.getStatus() != 200) {
                // all error responses have plaintext body.
                return response;
            }
            handleEncryptedResponse(response);
            return response;
        }
        return nextFilter(httpRequest);
    }

    private void handleEncryptedResponse(HttpResponse response) {
        decryptResponseBody(response);
        changeMimeType(response);
    }

    private void signAndEncryptRequest(HttpRequest httpRequest) {
        serialize(httpRequest);
        signRequest(httpRequest);
        encryptRequestBody(httpRequest);
    }

    private boolean shouldEncrypt(HttpRequest httpRequest) {
        return Boolean.parseBoolean(
                httpRequest.getHeaders().get(HeaderKeys.CYPHER).stream()
                        .findAny()
                        .orElse(Boolean.FALSE)
                        .toString());
    }

    private void serialize(HttpRequest request) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            request.setBody(mapper.writeValueAsString(request.getBody()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void signRequest(HttpRequest request) {
        request.getHeaders()
                .add(
                        HeaderKeys.SIGNATURE,
                        CryptoUtils.calculateRequestSignature(
                                request.getBody().toString(),
                                request.getURI().getPath(),
                                request.getMethod().name()));
    }

    private void encryptRequestBody(HttpRequest request) {
        request.setBody(CryptoUtils.encryptRequest(request.getBody().toString()));
    }

    private void decryptResponseBody(HttpResponse response) {
        response.getInternalResponse()
                .setEntityInputStream(
                        new ByteArrayInputStream(
                                CryptoUtils.decryptResponse(response.getBody(String.class))));
    }

    private void changeMimeType(HttpResponse response) {
        response.getHeaders().remove(HeaderKeys.CONTENT);
        response.getHeaders().add(HeaderKeys.CONTENT, MediaType.APPLICATION_JSON);
    }
}
