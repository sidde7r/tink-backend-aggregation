package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey;

import java.util.Date;
import java.util.List;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc.AddDeviceRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc.AddDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc.ConfirmTanCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc.ConfirmTanCodeResponse;
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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.rpc.CrossKeyResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.sessionhandler.rpc.KeepAliveResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.sessionhandler.rpc.LogoutResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class CrossKeyApiClient {
    private final TinkHttpClient client;
    private final CrossKeyConfiguration agentConfiguration;

    public CrossKeyApiClient(TinkHttpClient client, CrossKeyConfiguration agentConfiguration) {
        this.client = client;
        this.agentConfiguration = agentConfiguration;
    }

    public CrossKeyResponse initSession() {
        return buildRequest(CrossKeyConstants.Url.SYSTEM_STATUS_URI)
                .queryParam(CrossKeyConstants.Query.APP_ID, CrossKeyConstants.AutoAuthentication.APP_VERSION)
                .queryParam(CrossKeyConstants.Query.LANGUAGE, CrossKeyConstants.AutoAuthentication.LANGUAGE)
                .get(CrossKeyResponse.class);
    }

    public LoginWithoutTokenResponse loginUsernamePassword(LoginWithoutTokenRequest request) {
        return buildRequest(CrossKeyConstants.Url.LOGIN_WITH_USERNAME_PASSWORD)
                .post(LoginWithoutTokenResponse.class, request);
    }

    public LoginWithTokenResponse loginWithToken(LoginWithTokenRequest loginRequest) {
        return buildRequest(CrossKeyConstants.Url.LOGIN_WITH_TOKEN)
                .post(LoginWithTokenResponse.class, loginRequest);
    }

    public ConfirmTanCodeResponse confirmTanCode(ConfirmTanCodeRequest request) {
        return buildRequest(CrossKeyConstants.Url.CONFIRM_TAN_CODE)
                .post(ConfirmTanCodeResponse.class, request);
    }

    public AddDeviceResponse addDevice(AddDeviceRequest request) {
        return buildRequest(CrossKeyConstants.Url.ADD_DEVICE)
                .post(AddDeviceResponse.class, request);
    }

    public AccountsResponse fetchAccounts() {
        return buildRequest(CrossKeyConstants.Url.FETCH_ACCOUNTS)
                .queryParam(CrossKeyConstants.Query.SHOW_HIDDEN, CrossKeyConstants.Query.VALUE_TRUE)
                .get(AccountsResponse.class);
    }

    public TransactionsResponse fetchTransactions(Account account, Date fromDate, Date toDate) {
        return buildRequest(CrossKeyConstants.Url.FETCH_TRANSACTIONS)
                .queryParam(CrossKeyConstants.Query.ACCOUNT_ID, account.getBankIdentifier())
                .queryParam(CrossKeyConstants.Query.FROM_DATE, format(fromDate))
                .queryParam(CrossKeyConstants.Query.TO_DATE, format(toDate))
                .get(TransactionsResponse.class);
    }

    private static String format(Date date) {
        return date != null ?
                ThreadSafeDateFormat.FORMATTER_INTEGER_DATE.format(date) :
                "";
    }

    public LogoutResponse logout() {
        return buildRequest(CrossKeyConstants.Url.LOGOUT)
                .get(LogoutResponse.class);
    }

    public KeepAliveResponse keepAlive() {
        return buildRequest(CrossKeyConstants.Url.KEEPALIVE)
                .get(KeepAliveResponse.class);
    }

    public LoanDetailsResponse fetchLoanDetails(CrossKeyAccount account) {
        return buildRequest(CrossKeyConstants.Url.FETCH_LOANDETAILS)
                .queryParam(CrossKeyConstants.Query.LOAN_ACCOUNT_ID, account.getAccountId())
                .get(LoanDetailsResponse.class);
    }

    public CardsResponse fetchCards(CardsRequest request) {
        return buildRequest(CrossKeyConstants.Url.FETCH_GETCARDS)
                .post(CardsResponse.class, request);
    }

    public CrossKeyCard fetchCard(String cardId) {
        return buildRequest(CrossKeyConstants.Url.FETCH_GETCARD)
                .queryParam(CrossKeyConstants.Query.ID, cardId)
                .get(CrossKeyCard.class);
    }

    public List<CreditCardTransactionEntity> fetchCreditCardTransactions(String cardId, Date fromDate, Date toDate) {
        return buildRequest(CrossKeyConstants.Url.FETCH_CARD_TRANSACTIONS)
                .queryParam(CrossKeyConstants.Query.CARD_ID, cardId)
                .queryParam(CrossKeyConstants.Query.FROM_DATE, format(fromDate))
                .queryParam(CrossKeyConstants.Query.TO_DATE, format(toDate))
                .get(CreditCardTransactionsResponse.class).getCreditTransactions();
    }

    public PortfolioResponse fetchPortfolio(String accountId) {
        return buildRequest(CrossKeyConstants.Url.FETCH_PORTFOLIO)
                .post(PortfolioResponse.class, PortfolioRequest.withAccountId(accountId));
    }

    public String fetchPortfolioAsString(String accountId) {
        return buildRequest(CrossKeyConstants.Url.FETCH_PORTFOLIO)
                .post(String.class, PortfolioRequest.withAccountId(accountId));
    }

    public InstrumentDetailsResponse fetchInstrumentDetails(String isinCode, String marketPlace) {
        return buildRequest(CrossKeyConstants.Url.FETCH_INSTRUMENT_DETAILS)
                .post(InstrumentDetailsResponse.class, InstrumentDetailsRequest.of(isinCode, marketPlace));
    }

    public FundInfoResponse fetchFundInfo(String fundCode) {
        return buildRequest(CrossKeyConstants.Url.FETCH_FUND_INFO)
                .queryParam(CrossKeyConstants.Query.FUND_CODE, fundCode)
                .get(FundInfoResponse.class);
    }

    private RequestBuilder buildRequest(String path) {
        return client
                .request(CrossKeyConstants.Url.getUrl(agentConfiguration.getBaseUrl(), path))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }
}
