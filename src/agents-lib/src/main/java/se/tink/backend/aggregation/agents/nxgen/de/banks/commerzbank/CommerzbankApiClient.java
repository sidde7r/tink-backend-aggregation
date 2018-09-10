package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.LoginRequestBody;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities.ResultEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities.RootModel;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.entities.TransactionModel;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.entities.TransactionResultEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.rpc.Identifier;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.rpc.SearchCriteriaDto;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.rpc.TransactionRequestBody;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.session.entities.SessionModel;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CommerzbankApiClient {

    private final TinkHttpClient client;
    private static final AggregationLogger LOGGER = new AggregationLogger(CommerzbankApiClient.class);

    public CommerzbankApiClient(TinkHttpClient client) {
        this.client = client;
    }

    private static URL getUrl(String resource) {
        return new URL(CommerzbankConstants.URLS.HOST + resource);
    }

    private RequestBuilder firstRequest() {
        return client.request(getUrl(CommerzbankConstants.URLS.LOGIN))
                .header(CommerzbankConstants.HEADERS.CONTENT_TYPE, CommerzbankConstants.VALUES.JSON);
    }

    private RequestBuilder makeRequest(String resource) {
        return client.request(getUrl(resource))
                .header(CommerzbankConstants.HEADERS.CONTENT_TYPE, CommerzbankConstants.VALUES.JSON)
                .header(CommerzbankConstants.HEADERS.CCB_CLIENT_VERSION, CommerzbankConstants.VALUES.CCB_VALUE)
                .header(CommerzbankConstants.HEADERS.USER_AGENT, CommerzbankConstants.VALUES.USER_AGENT_VALUE);
    }

    public HttpResponse login(String username, String password) throws JsonProcessingException {

        LoginRequestBody loginRequestBody = new LoginRequestBody(username, password,
                CommerzbankConstants.VALUES.SESSION_TOKEN_VALUE);
        String serialized = new ObjectMapper().writeValueAsString(loginRequestBody);

        return firstRequest().post(HttpResponse.class, serialized);
    }

    public ResultEntity financialOverview() {
        String resultString = makeRequest(CommerzbankConstants.URLS.OVERVIEW).post(String.class);
        LOGGER.infoExtraLong(resultString, CommerzbankConstants.LOGTAG.FINANCE_OVERVIEW);
        try {
            return new ObjectMapper().readValue(resultString, RootModel.class).getResult();
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public SessionModel logout() {
        return makeRequest(CommerzbankConstants.URLS.LOGOUT).get(SessionModel.class);
    }

    public HttpResponse keepAlive() {
        return makeRequest(CommerzbankConstants.URLS.OVERVIEW)
                .post(HttpResponse.class);
    }

    public void logMultibankingProducts() {
        try {
            LOGGER.infoExtraLong(makeRequest(CommerzbankConstants.URLS.MULTIBANKING).post(String.class),
                    CommerzbankConstants.LOGTAG.MULTIBANKING_PRODUCTS);
        } catch (Exception e) {
            LOGGER.warnExtraLong(e.getMessage(), CommerzbankConstants.LOGTAG.MULTIBANKING_ERROR);
        }
    }

    public TransactionResultEntity transactionOverview(String productType, String identifier, int page, String productBranch)
            throws JsonProcessingException {
        TransactionRequestBody transactionRequestBody = new TransactionRequestBody(
                new SearchCriteriaDto(null, null, page, CommerzbankConstants.VALUES.AMOUNT_TYPE,
                        30, null),
                new Identifier(productType, CommerzbankConstants.VALUES.CURRENCY_VALUE, identifier,
                        productBranch));
        String serialized = new ObjectMapper().writeValueAsString(transactionRequestBody);

        TransactionResultEntity result = makeRequest(CommerzbankConstants.URLS.TRANSACTIONS)
                .post(TransactionModel.class, serialized).getResult();

        LOGGER.infoExtraLong(SerializationUtils.serializeToString(result),
                CommerzbankConstants.LOGTAG.TRANSACTION_LOGGING);

        return result;
    }

}
