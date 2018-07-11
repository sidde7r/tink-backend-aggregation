package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.LoginRequestBody;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities.ResultEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities.RootModel;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.entities.TransactionModel;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.entities.TransactionResultEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.rpc.Identifier;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.rpc.SearchCriteriaDto;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.rpc.TransactionRequestBody;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class CommerzbankApiClient {

    private final TinkHttpClient client;
    private final SessionStorage storage;

    public CommerzbankApiClient(TinkHttpClient client, SessionStorage storage) {
        this.client = client;
        this.storage = storage;
    }

    private static URL getUrl(String resource) {
        return new URL(CommerzbankConstants.URLS.HOST + resource);
    }

    private RequestBuilder firstRequest(String resource) {
        return client.request(getUrl(resource))
                .header(CommerzbankConstants.HEADERS.CONTENT_TYPE, CommerzbankConstants.VALUES.JSON);
    }

    private RequestBuilder makeRequest(String resource) {
        return client.request(getUrl(resource))
                .header(CommerzbankConstants.HEADERS.CONTENT_TYPE, CommerzbankConstants.VALUES.JSON)
                .header(CommerzbankConstants.HEADERS.COOKIE, storage.get(CommerzbankConstants.HEADERS.COOKIE))
                .header(CommerzbankConstants.HEADERS.CCB_CLIENT_VERSION, CommerzbankConstants.VALUES.CCB_VALUE)
                .header(CommerzbankConstants.HEADERS.USER_AGENT, CommerzbankConstants.VALUES.USER_AGENT_VALUE);
    }

    public HttpResponse login(String username, String password) throws JsonProcessingException {

        LoginRequestBody loginRequestBody = new LoginRequestBody(username, password,
                CommerzbankConstants.VALUES.SESSION_TOKEN_VALUE);
        String serialized = new ObjectMapper().writeValueAsString(loginRequestBody);

        return firstRequest(CommerzbankConstants.URLS.LOGIN)
                .post(HttpResponse.class, serialized);
    }

    public ResultEntity financialOverview() {
        return makeRequest(CommerzbankConstants.URLS.OVERVIEW).post(RootModel.class).getResult();
    }

    public HttpResponse keepAlive() {
        return makeRequest(CommerzbankConstants.URLS.OVERVIEW)
                .post(HttpResponse.class);
    }

    public TransactionResultEntity transactionOverview(String productType, String identifier)
            throws JsonProcessingException {

        TransactionRequestBody transactionRequestBody = new TransactionRequestBody(
                new SearchCriteriaDto(null, null, 0, CommerzbankConstants.VALUES.AMOUNT_TYPE,
                        30, null),
                new Identifier(productType, CommerzbankConstants.VALUES.CURRENCY_VALUE, identifier,
                        CommerzbankConstants.VALUES.PRODUCT_BRANCH));
        String serialized = new ObjectMapper().writeValueAsString(transactionRequestBody);

        return makeRequest(CommerzbankConstants.URLS.TRANSACTIONS)
                .post(TransactionModel.class, serialized).getResult();
    }
}
