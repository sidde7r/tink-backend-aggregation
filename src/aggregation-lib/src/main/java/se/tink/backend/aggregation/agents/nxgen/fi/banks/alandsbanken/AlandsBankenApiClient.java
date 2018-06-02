package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken;

import java.util.Date;
import java.util.List;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.authenticator.rpc.AddDeviceRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.authenticator.rpc.AddDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.authenticator.rpc.ConfirmTanCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.authenticator.rpc.ConfirmTanCodeResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.authenticator.rpc.LoginWithTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.authenticator.rpc.LoginWithTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.authenticator.rpc.LoginWithoutTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.authenticator.rpc.LoginWithoutTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.creditcard.entities.CreditCardTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.creditcard.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.entities.AlandsBankenAccount;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.entities.AlandsBankenCard;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.investment.rpc.FundInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.investment.rpc.InstrumentDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.investment.rpc.InstrumentDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.investment.rpc.PortfolioRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.investment.rpc.PortfolioResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.rpc.CardsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.sessionhandler.rpc.KeepAliveResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.sessionhandler.rpc.LogoutResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class AlandsBankenApiClient {
    private final TinkHttpClient client;

    public AlandsBankenApiClient(TinkHttpClient client) {
        this.client = client;
    }

    public LoginWithoutTokenResponse loginWithoutToken(LoginWithoutTokenRequest request) {
        return buildRequest(AlandsBankenConstants.Url.LOGIN_WITHOUT_TOKEN)
                .post(LoginWithoutTokenResponse.class, request);
    }

    public LoginWithTokenResponse loginWithToken(LoginWithTokenRequest loginRequest) {
        return buildRequest(AlandsBankenConstants.Url.LOGIN_WITH_TOKEN)
                .post(LoginWithTokenResponse.class, loginRequest);
    }

    public ConfirmTanCodeResponse confirmTanCode(ConfirmTanCodeRequest request) {
        return buildRequest(AlandsBankenConstants.Url.CONFIRM_TAN_CODE)
                .post(ConfirmTanCodeResponse.class, request);
    }

    public AddDeviceResponse addDevice(AddDeviceRequest request) {
        return buildRequest(AlandsBankenConstants.Url.ADD_DEVICE)
                .post(AddDeviceResponse.class, request);
    }

    public AccountsResponse fetchAccounts() {
        return buildRequest(AlandsBankenConstants.Url.FETCH_ACCOUNTS)
                .queryParam("showHidden", "true")
                .get(AccountsResponse.class);
    }

    public TransactionsResponse fetchTransactions(Account account, Date fromDate, Date toDate) {
        return buildRequest(AlandsBankenConstants.Url.FETCH_TRANSACTIONS)
                .queryParam("accountId", account.getBankIdentifier())
                .queryParam("fromdate", format(fromDate))
                .queryParam("todate", format(toDate))
                .get(TransactionsResponse.class);
    }

    private static String format(Date date) {
        return date != null ?
                ThreadSafeDateFormat.FORMATTER_INTEGER_DATE.format(date) :
                "";
    }

    public LogoutResponse logout() {
        return buildRequest(AlandsBankenConstants.Url.LOGOUT)
                .get(LogoutResponse.class);
    }

    public KeepAliveResponse keepAlive() {
        return buildRequest(AlandsBankenConstants.Url.KEEPALIVE)
                .get(KeepAliveResponse.class);
    }

    public LoanDetailsResponse fetchLoanDetails(AlandsBankenAccount account) {
        return buildRequest(AlandsBankenConstants.Url.FETCH_LOANDETAILS)
                .queryParam("loanAccountId", account.getAccountId())
                .get(LoanDetailsResponse.class);
    }

    public List<AlandsBankenCard> fetchCards() {
        return buildRequest(AlandsBankenConstants.Url.FETCH_GETCARDS)
                .get(CardsResponse.class).getCards();
    }

    public AlandsBankenCard fetchCard(String cardId) {
        return buildRequest(AlandsBankenConstants.Url.FETCH_GETCARD)
                .queryParam("id", cardId)
                .get(AlandsBankenCard.class);
    }

    public List<CreditCardTransactionEntity> fetchCreditCardTransactions(String cardId, Date fromDate, Date toDate) {
        return buildRequest(AlandsBankenConstants.Url.FETCH_CARD_TRANSACTIONS)
                .queryParam("cardId", cardId)
                .queryParam("fromDate", format(fromDate))
                .queryParam("toDate", format(toDate))
                .get(CreditCardTransactionsResponse.class).getCreditTransactions();
    }

    public PortfolioResponse fetchPortfolio(String accountId) {
        return buildRequest(AlandsBankenConstants.Url.FETCH_PORTFOLIO)
                .post(PortfolioResponse.class, PortfolioRequest.withAccountId(accountId));
    }

    public String fetchPortfolioAsString(String accountId) {
        return buildRequest(AlandsBankenConstants.Url.FETCH_PORTFOLIO)
                .post(String.class, PortfolioRequest.withAccountId(accountId));
    }

    public InstrumentDetailsResponse fetchInstrumentDetails(String isinCode, String marketPlace) {
        return buildRequest(AlandsBankenConstants.Url.FETCH_INSTRUMENT_DETAILS)
                .post(InstrumentDetailsResponse.class, InstrumentDetailsRequest.of(isinCode, marketPlace));
    }

    public FundInfoResponse fetchFundInfo(String fundCode) {
        return buildRequest(AlandsBankenConstants.Url.FETCH_FUND_INFO)
                .queryParam("fundCode", fundCode)
                .get(FundInfoResponse.class);
    }

    private RequestBuilder buildRequest(URL url) {
        return client
                .request(url)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }
}
