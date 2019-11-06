package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa;

import java.time.LocalDate;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.CaixaConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.CaixaConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.CaixaConstants.Parameters;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.CaixaConstants.QueryParams;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.CaixaConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.CaixaConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class CaixaApiClient {

    private final TinkHttpClient httpClient;

    private static final String ENCODED_SPACE = "%20";

    public CaixaApiClient(TinkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void authenticate(String login, String password) {
        createBaseRequest(Urls.AUTH)
                .queryParam(QueryParams.USER, login)
                .addBasicAuth(login, password)
                .post(HttpResponse.class);
    }

    public AccountsResponse fetchAccounts() {
        return createBaseRequest(Urls.FETCH_ACCOUNTS)
                .queryParam(
                        QueryParams.TARGET_OPERATION_TYPE,
                        QueryValues.BALANCES_AND_TRANSACTIONS_OPERATION)
                .get(AccountsResponse.class);
    }

    public AccountDetailsResponse fetchBalance(String accountKey) {
        return createBaseRequest(buildFetchAccountDetailsUrl(accountKey))
                .queryParam(QueryParams.INCLUDE_BALANCES, Boolean.TRUE.toString())
                .get(AccountDetailsResponse.class);
    }

    public AccountDetailsResponse fetchTransactions(
            String accountKey, int page, LocalDate from, LocalDate to) {
        return createBaseRequest(buildFetchAccountDetailsUrl(accountKey))
                .queryParam(QueryParams.INCLUDE_TRANSACTIONS, Boolean.TRUE.toString())
                .queryParam(QueryParams.FROM, CaixaConstants.DATE_FORMATTER.format(from))
                .queryParam(QueryParams.TO, CaixaConstants.DATE_FORMATTER.format(to))
                .get(AccountDetailsResponse.class);
    }

    private URL buildFetchAccountDetailsUrl(String accountKey) {
        return new URL(
                Urls.FETCH_ACCOUNT_DETAILS
                        .parameter(Parameters.ACCOUNT_KEY, accountKey)
                        .get()
                        .replace("+", ENCODED_SPACE));
    }

    private RequestBuilder createBaseRequest(URL url) {
        return httpClient
                .request(url)
                .accept(HeaderValues.ACCEPT)
                .header(HeaderKeys.X_CGD_APP_DEVICE, HeaderValues.DEVICE_NAME)
                .header(HeaderKeys.X_CGD_APP_NAME, HeaderValues.APP_NAME)
                .header(HeaderKeys.X_CGD_APP_VERSION, HeaderValues.APP_VERSION);
    }
}
