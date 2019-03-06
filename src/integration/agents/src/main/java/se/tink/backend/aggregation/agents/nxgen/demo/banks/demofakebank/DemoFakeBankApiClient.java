package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank;

import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank.authenticator.DemoFakeBankAuthenticateResponse;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank.authenticator.rpc.DemoFakeBankAuthenticationBody;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;

import javax.ws.rs.core.MediaType;
import java.util.Collections;

public class DemoFakeBankApiClient {
    private final TinkHttpClient client;

    public DemoFakeBankApiClient(TinkHttpClient client) {
        this.client = client;
    }

    public DemoFakeBankAuthenticateResponse authenticate(DemoFakeBankAuthenticationBody authenticationBody) {
        return createRequest(DemoFakeBankConstants.Urls.AUTHENTICATE_URL)
                .post(DemoFakeBankAuthenticateResponse.class, authenticationBody);
    }

    private RequestBuilder createRequest(URL url) {
        return client
                .request(url)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }

    public FakeAccounts fetchAccounts() {
        return new FakeAccounts(Collections.singletonList(
                new FakeAccount("TransactionalAccount", 200, "15629906")));
        //TODO: get real accounts from bank
    }
}
