package se.tink.backend.aggregation.agents.nxgen.demo.banks.demoFI;

import se.tink.backend.aggregation.agents.nxgen.demo.banks.demoFI.authenticator.DemoFIAuthenticateResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;

import javax.ws.rs.core.MediaType;

public class DemoFIApiClient {
    private final TinkHttpClient client;

    public DemoFIApiClient(TinkHttpClient client) {
        this.client = client;
    }

    public DemoFIAuthenticateResponse authenticate() {
        return createRequest(DemoFIConstants.Urls.AUTHENTICATE_URL)
                .post(DemoFIAuthenticateResponse.class);
    }

    private RequestBuilder createRequest(URL url) {
        return client
                .request(url)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }
}
