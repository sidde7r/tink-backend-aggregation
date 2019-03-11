package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank;

import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank.authenticator.DemoFakeBankAuthenticateResponse;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank.authenticator.rpc.DemoFakeBankAuthenticationBody;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank.fetcher.transactionalaccount.rpc.DemoFakeBankAccountBody;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank.fetcher.transactionalaccount.rpc.DemoFakeBankAccountsResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;

import javax.ws.rs.core.MediaType;

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

    public DemoFakeBankAccountsResponse fetchAccounts(String username, String token) {
        return createRequest(DemoFakeBankConstants.Urls.ACCOUNTS_URL)
                .post(DemoFakeBankAccountsResponse.class, new DemoFakeBankAccountBody(username, token));

    }
}
