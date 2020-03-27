package se.tink.backend.aggregation.agents.nxgen.de.banks.fints;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.base64.FinTsBase64;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.FinTsRequest;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.FinTsResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

@Slf4j
class FinTsRequestSender {

    private final TinkHttpClient httpClient;
    private final String endpoint;

    public FinTsRequestSender(TinkHttpClient httpClient, String endpoint) {
        this.httpClient = httpClient;
        this.endpoint = endpoint;
    }

    public FinTsResponse sendRequest(FinTsRequest request) {
        log.info("Request: {}", request.toFinTsFormat());
        String b64Response = send(FinTsBase64.encodeRequestToBase64(request));

        String plainResponse = FinTsBase64.decodeResponseFromBase64(b64Response);
        log.info("Response: {}", plainResponse.replaceAll("\u0000", "0"));
        FinTsResponse response = new FinTsResponse(plainResponse);

        return response;
    }

    private String send(String request) {
        return httpClient.request(endpoint).post(String.class, request);
    }
}
