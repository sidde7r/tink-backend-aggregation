package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp;

import com.google.common.base.Preconditions;
import java.util.List;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.authentication.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.authentication.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.authentication.rpc.RegisterDeviceRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.authentication.rpc.RegisterDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.creditcard.entities.CreditCardEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.creditcard.rpc.CreditCardDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.creditcard.rpc.CreditCardsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.loan.entities.LoanDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.loan.rpc.LoanDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.loan.rpc.LoansResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.transactionalaccount.entities.AccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.transactionalaccount.rpc.TransactionDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.transactionalaccount.rpc.TransactionDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.transactionalaccount.rpc.TransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.rpc.OmaspErrorResponse;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class OmaspApiClient {

    private final TinkHttpClient httpClient;
    private final SessionStorage sessionStorage;

    public OmaspApiClient(TinkHttpClient httpClient, SessionStorage sessionStorage) {
        this.httpClient = httpClient;
        this.sessionStorage = sessionStorage;
    }

    private RequestBuilder buildRequest(URL url) {
        return httpClient.request(url)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }

    public OmaspErrorResponse getError(HttpResponse response) {
        return response.getBody(OmaspErrorResponse.class);
    }

    public LoginResponse login(String username, String password) {
        return login(username, password, null);
    }

    public LoginResponse login(String username, String password, String deviceId) {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUserId(username)
                .setPassword(password)
                .setDeviceId(deviceId);

        return buildRequest(OmaspConstants.Url.LOGIN).post(LoginResponse.class, loginRequest);
    }

    public RegisterDeviceResponse registerDevice(String codeCardId, String codeCardIndex, String codeCardValue) {
        RegisterDeviceRequest registerDeviceRequest = new RegisterDeviceRequest();
        registerDeviceRequest.setCardId(codeCardId)
                .setSecurityKeyIndex(codeCardIndex)
                .setSecurityCode(codeCardValue);

        return buildRequest(OmaspConstants.Url.REGISTER_DEVICE)
                .post(RegisterDeviceResponse.class, registerDeviceRequest);
    }

    public List<AccountsEntity> getAccounts() {
        AccountsResponse accountsResponse = buildRequest(OmaspConstants.Url.ACCOUNTS).get(AccountsResponse.class);
        return accountsResponse.getAccounts();
    }

    public TransactionsResponse getTransactionsFor(String accountId) {
        TransactionsRequest transactionsRequest = new TransactionsRequest();
        transactionsRequest.setAccountId(accountId);

        return buildRequest(OmaspConstants.Url.TRANSACTIONS).post(TransactionsResponse.class,
                transactionsRequest);
    }

    public TransactionDetailsResponse getTransactionDetails(String accountId, String transactionId) {
        Preconditions.checkState(sessionStorage.containsKey(OmaspConstants.Storage.ACCESS_TOKEN),
                "Has no access token");

        String accessToken = sessionStorage.get(OmaspConstants.Storage.ACCESS_TOKEN);

        TransactionDetailsRequest transactionDetailsRequest = new TransactionDetailsRequest();
        transactionDetailsRequest.setAccept("application/json")
                .setAccountId(accountId)
                .setAuthorization(accessToken);

        return buildRequest(OmaspConstants.Url.TRANSACTION_DETAILS.parameter("transactionId", transactionId))
                .post(TransactionDetailsResponse.class, transactionDetailsRequest);
    }

    public List<CreditCardEntity> getCreditCards() {
        return buildRequest(OmaspConstants.Url.CREDITCARDS).get(CreditCardsResponse.class).getCards();
    }

    public CreditCardDetailsResponse getCreditCardDetails(String cardId) {
        return buildRequest(OmaspConstants.Url.CREDITCARD_DETAILS.parameter("cardId", cardId))
                .get(CreditCardDetailsResponse.class);
    }

    public List<LoanEntity> getLoans() {
        return buildRequest(OmaspConstants.Url.LOANS).get(LoansResponse.class).getLoans();
    }

    public LoanDetailsEntity getLoanDetails(String loanId) {
        LoanDetailsRequest loanDetailsRequest = new LoanDetailsRequest(loanId);
        return buildRequest(OmaspConstants.Url.LOAN_DETAILS).post(LoanDetailsResponse.class, loanDetailsRequest).getLoan();
    }
}
