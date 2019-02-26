package se.tink.backend.aggregation.agents.nxgen.demo.banks.demoFI;

import se.tink.backend.aggregation.agents.nxgen.demo.banks.demoFI.authenticator.DFIAuthenticateResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;

import javax.ws.rs.core.MediaType;

public class DFIApiClient {
    private final TinkHttpClient client;

    public DFIApiClient(TinkHttpClient client) {
        this.client = client;
    }

    public DFIAuthenticateResponse authenticate() {
        return createRequest(DFIConstants.Urls.AUTHENTICATE_URL)
                .post(DFIAuthenticateResponse.class);
    }

    private RequestBuilder createRequest(URL url) {
        return client
                .request(url)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }
}
