package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.LoginRequestBody;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities.ResultEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities.RootModel;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.entities.TransactionModel;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.entities.TransactionResultEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.rpc.Identifier;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.rpc.SearchCriteriaDto;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.rpc.TransactionRequestBody;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.session.entities.SessionModel;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;

public class CommerzbankApiClient {

    private final TinkHttpClient client;

    public CommerzbankApiClient(TinkHttpClient client) {
        this.client = client;
    }

    private static URL getUrl(String resource) {
        return new URL(CommerzbankConstants.URLS.HOST + resource);
    }

    private RequestBuilder firstRequest() {
        return client.request(getUrl(CommerzbankConstants.URLS.LOGIN))
                .header(
                        CommerzbankConstants.HEADERS.CONTENT_TYPE,
                        CommerzbankConstants.VALUES.JSON);
    }

    private RequestBuilder makeRequest(String resource) {
        return client.request(getUrl(resource))
                .header(CommerzbankConstants.HEADERS.CONTENT_TYPE, CommerzbankConstants.VALUES.JSON)
                .header(
                        CommerzbankConstants.HEADERS.CCB_CLIENT_VERSION,
                        CommerzbankConstants.VALUES.CCB_VALUE)
                .header(
                        CommerzbankConstants.HEADERS.USER_AGENT,
                        CommerzbankConstants.VALUES.USER_AGENT_VALUE);
    }

    public HttpResponse login(String username, String password) throws JsonProcessingException {

        LoginRequestBody loginRequestBody =
                new LoginRequestBody(
                        username, password, CommerzbankConstants.VALUES.SESSION_TOKEN_VALUE);
        String serialized = new ObjectMapper().writeValueAsString(loginRequestBody);

        return firstRequest().post(HttpResponse.class, serialized);
    }

    public ResultEntity financialOverview() {
        return makeRequest(CommerzbankConstants.URLS.OVERVIEW).post(RootModel.class).getResult();
    }

    public SessionModel logout() {
        return makeRequest(CommerzbankConstants.URLS.LOGOUT).get(SessionModel.class);
    }

    public HttpResponse keepAlive() {
        return makeRequest(CommerzbankConstants.URLS.OVERVIEW).post(HttpResponse.class);
    }

    private String toCommerzDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(CommerzbankConstants.DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone(CommerzbankConstants.TIMEZONE_CET));
        return sdf.format(date);
    }

    public TransactionResultEntity fetchAllPages(
            Date fromDate, Date toDate, String productType, String identifier, String productBranch)
            throws JsonProcessingException {

        TransactionResultEntity transactionResultEntity =
                transactionOverview(productType, identifier, fromDate, toDate, productBranch, 0);
        int page = 1;
        while (transactionResultEntity != null && transactionResultEntity.canFetchMore(page)) {
            transactionResultEntity.addAll(
                    transactionOverview(
                            productType, identifier, fromDate, toDate, productBranch, page));

            page++;
        }
        return transactionResultEntity;
    }

    private TransactionResultEntity transactionOverview(
            String productType,
            String identifier,
            Date fromdate,
            Date toDate,
            String productBranch,
            int page)
            throws JsonProcessingException {

        TransactionRequestBody transactionRequestBody =
                new TransactionRequestBody(
                        new SearchCriteriaDto(
                                toCommerzDate(fromdate),
                                toCommerzDate(toDate),
                                page,
                                CommerzbankConstants.VALUES.AMOUNT_TYPE,
                                50,
                                null),
                        new Identifier(
                                productType,
                                CommerzbankConstants.VALUES.CURRENCY_VALUE,
                                identifier,
                                productBranch));
        String serialized = new ObjectMapper().writeValueAsString(transactionRequestBody);

        return makeRequest(CommerzbankConstants.URLS.TRANSACTIONS)
                .post(TransactionModel.class, serialized)
                .getResult();
    }
}
