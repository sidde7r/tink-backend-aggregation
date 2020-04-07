package se.tink.backend.aggregation.agents.nxgen.de.banks.fints;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.base64.FinTsBase64;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.FinTsRequest;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.FinTsResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

@Slf4j
@AllArgsConstructor
public class FinTsRequestSender {

    private final TinkHttpClient httpClient;
    private final String endpoint;

    public FinTsResponse sendRequest(FinTsRequest request) {
        String b64Response = send(FinTsBase64.encodeRequestToBase64(request));
        String plainResponse = FinTsBase64.decodeResponseFromBase64(b64Response);
        return new FinTsResponse(plainResponse);
    }

    private String send(String request) {
        return httpClient.request(endpoint).post(String.class, request);
    }
}
