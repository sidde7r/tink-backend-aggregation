package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans;

import java.time.LocalDate;
import java.util.Locale;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.authenticator.rpc.bankid.AuthenticateResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.authenticator.rpc.bankid.InitBankIdRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.creditcards.rpc.CreditCardDataResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.creditcards.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.creditcards.rpc.CreditCardsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.transactionalaccounts.rpc.AccountTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.transactionalaccounts.rpc.SavingsAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.rpc.CustomerResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class VolvoFinansApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;

    public VolvoFinansApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    private RequestBuilder createRequest(URL url) {
        RequestBuilder requestBuilder =
                client.request(url)
                        .accept(MediaType.WILDCARD)
                        .acceptLanguage(Locale.US)
                        .header(
                                VolvoFinansConstants.Headers.HEADER_X_API_KEY,
                                VolvoFinansConstants.Headers.VALUE_X_API_KEY);

        if (sessionStorage.containsKey(VolvoFinansConstants.Headers.HEADER_BEARER_TOKEN)) {
            String bearerToken =
                    sessionStorage.get(VolvoFinansConstants.Headers.HEADER_BEARER_TOKEN);
            String valueBearerToken =
                    VolvoFinansConstants.Headers.VALUE_BEARER_TOKEN_PREFIX + bearerToken;
            requestBuilder.header(
                    VolvoFinansConstants.Headers.HEADER_BEARER_TOKEN, valueBearerToken);
        }
        return requestBuilder;
    }

    private RequestBuilder createPostRequest(URL url) {
        return createRequest(url).type(MediaType.APPLICATION_JSON);
    }

    /* Authentication */
    public HttpResponse loginBankIdInit(InitBankIdRequest initBankIdRequest) {
        return createPostRequest(VolvoFinansConstants.Urls.LOGIN_BANKID_INIT)
                .post(HttpResponse.class, initBankIdRequest);
    }

    public AuthenticateResponse loginBankIdPoll(String identificationId) {
        URL url = VolvoFinansConstants.Urls.LOGIN_BANKID_POLL;
        String parameter = VolvoFinansConstants.UrlParameters.IDENTIFICATION_ID;
        AuthenticateResponse response =
                createRequest(url.parameter(parameter, identificationId))
                        .get(AuthenticateResponse.class);

        if (response.getBankIdStatus().equals(BankIdStatus.DONE)) {
            /* Authentication successful. Store bearer token for this session. */
            sessionStorage.put(
                    VolvoFinansConstants.Headers.HEADER_BEARER_TOKEN, response.getBearerToken());
        }

        return response;
    }

    public void logout() {
        createRequest(VolvoFinansConstants.Urls.LOGOUT).delete(HttpResponse.class);
    }

    public CustomerResponse keepAlive() {
        return createRequest(VolvoFinansConstants.Urls.CUSTOMER).get(CustomerResponse.class);
    }

    /* CC Data */
    public CreditCardDataResponse creditCardData() {
        return createPostRequest(VolvoFinansConstants.Urls.CREDIT_CARD_DATA)
                .get(CreditCardDataResponse.class);
    }

    /* Accounts */
    public CreditCardsResponse creditCardAccounts() {
        return createRequest(VolvoFinansConstants.Urls.CREDIT_CARD_ACCOUNTS)
                .get(CreditCardsResponse.class);
    }

    public SavingsAccountsResponse savingsAccounts() {
        return createRequest(VolvoFinansConstants.Urls.SAVINGS_ACCOUNTS)
                .get(SavingsAccountsResponse.class);
    }

    /* Transactions */
    public CreditCardTransactionsResponse creditCardAccountTransactions(
            String accountId, LocalDate fromDate, LocalDate toDate, int limit, int offset) {
        URL url = VolvoFinansConstants.Urls.CREDIT_CARD_ACCOUNTS_TRANSACTIONS;
        RequestBuilder requestBuilder =
                createRequest(
                                url.parameter(
                                        VolvoFinansConstants.UrlParameters.ACCOUNT_ID, accountId))
                        .queryParam(
                                VolvoFinansConstants.QueryParameters.FROM_DATE, fromDate.toString())
                        .queryParam(VolvoFinansConstants.QueryParameters.TO_DATE, toDate.toString())
                        .queryParam(
                                VolvoFinansConstants.QueryParameters.LIMIT, Integer.toString(limit))
                        .queryParam(
                                VolvoFinansConstants.QueryParameters.OFFSET,
                                Integer.toString(offset));

        return requestBuilder.get(CreditCardTransactionsResponse.class);
    }

    public AccountTransactionsResponse savingsAccountTransactions(
            String accountId, LocalDate fromDate, LocalDate toDate, int limit, int offset) {
        URL url = VolvoFinansConstants.Urls.SAVINGS_ACCOUNTS_TRANSACTIONS;
        return createRequest(
                        url.parameter(VolvoFinansConstants.UrlParameters.ACCOUNT_ID, accountId))
                .queryParam(VolvoFinansConstants.QueryParameters.FROM_DATE, fromDate.toString())
                .queryParam(VolvoFinansConstants.QueryParameters.TO_DATE, toDate.toString())
                .queryParam(VolvoFinansConstants.QueryParameters.LIMIT, Integer.toString(limit))
                .queryParam(VolvoFinansConstants.QueryParameters.OFFSET, Integer.toString(offset))
                .get(AccountTransactionsResponse.class);
    }
}
