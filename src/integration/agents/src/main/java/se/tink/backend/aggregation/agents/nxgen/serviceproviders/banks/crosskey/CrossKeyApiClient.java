package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey;

import java.util.Date;
import java.util.List;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyConstants.Url;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc.AddDeviceRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc.AddDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc.BankIdAutostartTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc.BankiIdCollectRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc.BankiIdResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc.ConfirmTanCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc.ConfirmTanCodeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc.ContentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc.LoginProvidersResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc.LoginWithTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc.LoginWithTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc.LoginWithoutTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc.LoginWithoutTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.creditcard.entities.CreditCardTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.creditcard.rpc.CardsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.creditcard.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.entities.CrossKeyAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.entities.CrossKeyCard;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.investment.rpc.FundInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.investment.rpc.InstrumentDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.investment.rpc.InstrumentDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.investment.rpc.PortfolioRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.investment.rpc.PortfolioResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.rpc.CardsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.rpc.IdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.rpc.CrossKeyResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.sessionhandler.rpc.KeepAliveResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.sessionhandler.rpc.LogoutResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CrossKeyApiClient {
    private final TinkHttpClient client;
    private final CrossKeyConfiguration agentConfiguration;

    public CrossKeyApiClient(TinkHttpClient client, CrossKeyConfiguration agentConfiguration) {
        this.client = client;
        this.agentConfiguration = agentConfiguration;
    }

    public CrossKeyResponse initSession() {
        return get(
                buildRequest(CrossKeyConstants.Url.SYSTEM_STATUS_URI)
                        .queryParam(
                                CrossKeyConstants.Query.APP_ID,
                                CrossKeyConstants.AutoAuthentication.APP_VERSION)
                        .queryParam(
                                CrossKeyConstants.Query.LANGUAGE,
                                CrossKeyConstants.AutoAuthentication.LANGUAGE),
                CrossKeyResponse.class);
    }

    public BankIdAutostartTokenResponse initBankId() {
        return post(
                buildRequest(CrossKeyConstants.Url.LOGIN_WITH_BANKID),
                BankIdAutostartTokenResponse.class,
                null);
    }

    public LoginProvidersResponse getLoginProviders() {
        return get(
                buildRequest(CrossKeyConstants.Url.GET_LOGIN_PROVIDERS),
                LoginProvidersResponse.class);
    }

    public ContentResponse getContent() {
        return get(buildRequest(CrossKeyConstants.Url.GET_CONTENT), ContentResponse.class);
    }

    public BankiIdResponse collectBankId() {
        HttpResponse post = null;
        try {
            post =
                    agentConfiguration
                            .getAppVersion()
                            .map(
                                    v ->
                                            post(
                                                    buildRequest(
                                                            CrossKeyConstants.Url.COLLECT_BANKIID),
                                                    HttpResponse.class,
                                                    new BankiIdCollectRequest(v)))
                            .orElseGet(
                                    () ->
                                            post(
                                                    buildRequest(
                                                            CrossKeyConstants.Url.COLLECT_BANKIID),
                                                    HttpResponse.class,
                                                    null));
        } catch (HttpResponseException ex) {
            return deserializeResponse(
                    BankiIdResponse.class, ex.getResponse().getBody(String.class));
        }

        return deserializeResponse(BankiIdResponse.class, post.getBody(String.class));
    }

    public LoginWithoutTokenResponse loginUsernamePassword(LoginWithoutTokenRequest request) {
        return post(
                buildRequest(CrossKeyConstants.Url.LOGIN_WITH_USERNAME_PASSWORD),
                LoginWithoutTokenResponse.class,
                request);
    }

    public LoginWithTokenResponse loginWithToken(LoginWithTokenRequest loginRequest) {
        return post(
                buildRequest(CrossKeyConstants.Url.LOGIN_WITH_TOKEN),
                LoginWithTokenResponse.class,
                loginRequest);
    }

    public ConfirmTanCodeResponse confirmTanCode(ConfirmTanCodeRequest request) {
        return post(
                buildRequest(CrossKeyConstants.Url.CONFIRM_TAN_CODE),
                ConfirmTanCodeResponse.class,
                request);
    }

    public AddDeviceResponse addDevice(AddDeviceRequest request) {
        return post(
                buildRequest(CrossKeyConstants.Url.ADD_DEVICE), AddDeviceResponse.class, request);
    }

    public AccountsResponse fetchAccounts() {
        return get(
                buildRequest(CrossKeyConstants.Url.FETCH_ACCOUNTS)
                        .queryParam(
                                CrossKeyConstants.Query.SHOW_HIDDEN,
                                CrossKeyConstants.Query.VALUE_TRUE),
                AccountsResponse.class);
    }

    public TransactionsResponse fetchTransactions(Account account, Date fromDate, Date toDate) {
        return get(
                buildRequest(CrossKeyConstants.Url.FETCH_TRANSACTIONS)
                        .queryParam(CrossKeyConstants.Query.ACCOUNT_ID, account.getApiIdentifier())
                        .queryParam(CrossKeyConstants.Query.FROM_DATE, format(fromDate))
                        .queryParam(CrossKeyConstants.Query.TO_DATE, format(toDate)),
                TransactionsResponse.class);
    }

    private static String format(Date date) {
        return date != null ? ThreadSafeDateFormat.FORMATTER_INTEGER_DATE.format(date) : "";
    }

    public LogoutResponse logout() {
        return get(buildRequest(CrossKeyConstants.Url.LOGOUT), LogoutResponse.class);
    }

    public KeepAliveResponse keepAlive() {
        return get(buildRequest(CrossKeyConstants.Url.KEEPALIVE), KeepAliveResponse.class);
    }

    public LoanDetailsResponse fetchLoanDetails(CrossKeyAccount account) {
        return get(
                buildRequest(CrossKeyConstants.Url.FETCH_LOANDETAILS)
                        .queryParam(
                                CrossKeyConstants.Query.LOAN_ACCOUNT_ID, account.getAccountId()),
                LoanDetailsResponse.class);
    }

    public CardsResponse fetchCards(CardsRequest request) {
        return post(
                buildRequest(CrossKeyConstants.Url.FETCH_GETCARDS), CardsResponse.class, request);
    }

    public CrossKeyCard fetchCard(String cardId) {
        return get(
                buildRequest(CrossKeyConstants.Url.FETCH_GETCARD)
                        .queryParam(CrossKeyConstants.Query.ID, cardId),
                CrossKeyCard.class);
    }

    public List<CreditCardTransactionEntity> fetchCreditCardTransactions(
            String cardId, Date fromDate, Date toDate) {

        return get(
                        buildRequest(CrossKeyConstants.Url.FETCH_CARD_TRANSACTIONS)
                                .queryParam(CrossKeyConstants.Query.CARD_ID, cardId)
                                .queryParam(CrossKeyConstants.Query.FROM_DATE, format(fromDate))
                                .queryParam(CrossKeyConstants.Query.TO_DATE, format(toDate)),
                        CreditCardTransactionsResponse.class)
                .getCreditTransactions();
    }

    public PortfolioResponse fetchPortfolio(String accountId) {
        return post(
                buildRequest(CrossKeyConstants.Url.FETCH_PORTFOLIO),
                PortfolioResponse.class,
                PortfolioRequest.withAccountId(accountId));
    }

    public String fetchPortfolioAsString(String accountId) {
        return post(
                buildRequest(CrossKeyConstants.Url.FETCH_PORTFOLIO),
                String.class,
                PortfolioRequest.withAccountId(accountId));
    }

    public InstrumentDetailsResponse fetchInstrumentDetails(String isinCode, String marketPlace) {
        return post(
                buildRequest(CrossKeyConstants.Url.FETCH_INSTRUMENT_DETAILS),
                InstrumentDetailsResponse.class,
                InstrumentDetailsRequest.of(isinCode, marketPlace));
    }

    public IdentityDataResponse fetchIdentityData() {
        return get(buildRequest(Url.IDENTITY_DATA), IdentityDataResponse.class);
    }

    public FundInfoResponse fetchFundInfo(String fundCode) {
        return get(
                buildRequest(CrossKeyConstants.Url.FETCH_FUND_INFO)
                        .queryParam(CrossKeyConstants.Query.FUND_CODE, fundCode),
                FundInfoResponse.class);
    }

    private RequestBuilder buildRequest(String path) {
        return client.request(CrossKeyConstants.Url.getUrl(agentConfiguration.getBaseUrl(), path))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }

    private <T> T get(RequestBuilder request, Class<T> responseType) {
        HttpResponse httpResponse = request.get(HttpResponse.class);

        return deserializeResponse(responseType, httpResponse);
    }

    private <T> T post(RequestBuilder request, Class<T> responseType, Object requestBody) {
        HttpResponse httpResponse = null;

        if (requestBody != null) {
            httpResponse = request.post(HttpResponse.class, requestBody);
        } else {
            httpResponse = request.post(HttpResponse.class);
        }

        return deserializeResponse(responseType, httpResponse);
    }

    private <T> T deserializeResponse(Class<T> responseType, HttpResponse httpResponse) {
        if (responseType.equals(HttpResponse.class)) {
            return (T) httpResponse;
        }

        return deserializeResponse(responseType, httpResponse.getBody(String.class));
    }

    private <T> T deserializeResponse(Class<T> responseType, String response) {
        response = cleanResponseString(response);

        if (responseType.equals(String.class)) {
            return (T) response;
        }

        return SerializationUtils.deserializeFromString(response, responseType);
    }

    private String cleanResponseString(String response) {
        if (response == null) {
            return response;
        }

        int idx = response.indexOf('\n');
        if (idx >= 0) {
            return response.substring(idx + 1);
        }

        return response;
    }
}
