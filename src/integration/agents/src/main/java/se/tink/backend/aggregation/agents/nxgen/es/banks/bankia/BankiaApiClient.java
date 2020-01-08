package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia;

import com.google.common.base.Strings;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaConstants.Default;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.authenticator.rpc.RsaKeyResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.creditcard.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.creditcard.rpc.CardTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.creditcard.rpc.CardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities.DateEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.identitydata.rpc.IdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.investment.entities.DataHomeModelEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.investment.entities.InvestmentAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.investment.entities.ValueAccountIdentifierEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.investment.rpc.PositionWalletRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.investment.rpc.PositionWalletResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.entities.LoanAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.rpc.LoanDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.rpc.ContractsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities.AccountIdentifierEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities.PaginationDataEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities.SearchCriteriaEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.rpc.AccountTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.rpc.AcountTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BankiaApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;

    public BankiaApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    public List<AccountEntity> getAccounts() {
        return getServicesContracts().getAccounts();
    }

    public List<CardEntity> getCards() {
        return getServicesContracts().getCards();
    }

    public List<InvestmentAccountEntity> getInvestments() {
        return getServicesContracts().getInvestments();
    }

    private RequestBuilder createRequest(String url) {
        return client.request(url)
                .queryParam(
                        BankiaConstants.Query.CM_FORCED_DEVICE_TYPE, BankiaConstants.Default.JSON)
                .queryParam(BankiaConstants.Query.OIGID, BankiaConstants.Default.TRUE)
                .queryParam(
                        BankiaConstants.Query.J_GID_COD_APP, BankiaConstants.Default.LOWER_CASE_AM)
                .queryParam(
                        BankiaConstants.Query.J_GID_COD_DS, BankiaConstants.Default.UPPER_CASE_OIP)
                .queryParam(BankiaConstants.Query.ORIGEN, BankiaConstants.Default.UPPER_CASE_AM)
                .accept(MediaType.APPLICATION_JSON)
                .acceptLanguage(Default.ACCEPT_LANGUAGE);
    }

    private RequestBuilder createInSessionRequest(String url) {
        return client.request(url)
                .queryParam(BankiaConstants.Query.J_GID_COD_APP, BankiaConstants.Default.O3)
                .queryParam(
                        BankiaConstants.Query.J_GID_COD_DS, BankiaConstants.Default.LOWER_CASE_OIP)
                .queryParam(
                        BankiaConstants.Query.X_J_GID_COD_APP,
                        BankiaConstants.Default.LOWER_CASE_AM)
                .queryParam(
                        BankiaConstants.Query.CM_FORCED_DEVICE_TYPE, BankiaConstants.Default.JSON)
                .accept(MediaType.APPLICATION_JSON)
                .acceptLanguage(Default.ACCEPT_LANGUAGE);
    }

    public List<LoanAccountEntity> getLoans() {
        return getServicesContracts().getLoans();
    }

    public CardTransactionsResponse getCardTransactions(String cardNumberUnmasked, int limit) {
        CardTransactionsRequest request = CardTransactionsRequest.create(cardNumberUnmasked, limit);
        return createInSessionRequest(BankiaConstants.Url.CREDIT_CARD_TRANSACTIONS)
                .body(request, MediaType.APPLICATION_JSON)
                .post(CardTransactionsResponse.class);
    }

    private ContractsResponse getServicesContracts() {
        try {
            return createInSessionRequest(BankiaConstants.Url.SERVICES_CONTRACTS)
                    .queryParam(
                            BankiaConstants.Query.GROUP_BY_FAMILIA, BankiaConstants.Default.TRUE)
                    .queryParam(BankiaConstants.Query.ID_VISTA, BankiaConstants.Default._1)
                    .get(ContractsResponse.class);
        } catch (HttpResponseException exception) {
            if (exception.getResponse().getStatus() == HttpStatus.SC_FORBIDDEN) {
                RsaKeyResponse rsaKeyResponse =
                        exception.getResponse().getBody(RsaKeyResponse.class);
                if (!Objects.isNull(rsaKeyResponse)
                        && !Strings.isNullOrEmpty(rsaKeyResponse.getResponseUrl())) {
                    throw BankServiceError.BANK_SIDE_FAILURE.exception(exception);
                }
            }
            throw exception;
        }
    }

    public AcountTransactionsResponse getTransactions(
            Account account, @Nullable PaginationDataEntity paginationData) {
        AccountIdentifierEntity accountIdentifier = new AccountIdentifierEntity();
        accountIdentifier.setCountry(
                account.getFromTemporaryStorage(BankiaConstants.StorageKey.COUNTRY));
        accountIdentifier.setControlDigits(
                account.getFromTemporaryStorage(BankiaConstants.StorageKey.CONTROL_DIGITS));
        accountIdentifier.setIdentifier(account.getApiIdentifier());

        SearchCriteriaEntity searchCriteria = null;
        if (Objects.isNull(paginationData)) {
            // Specify search dates on first page request
            // Bankia allows fetching transactions since day 1 of 23 months ago
            searchCriteria = new SearchCriteriaEntity();
            searchCriteria.setDateOperationFrom(
                    DateEntity.of(LocalDate.now().minusMonths(23).withDayOfMonth(1)));
            searchCriteria.setOperationDateUntil(DateEntity.of(LocalDate.now()));
        }

        AccountTransactionsRequest request =
                AccountTransactionsRequest.create(
                        accountIdentifier,
                        searchCriteria,
                        BankiaConstants.LANGUAGE,
                        paginationData);

        return createInSessionRequest(BankiaConstants.Url.SERVICES_ACCOUNT_MOVEMENT)
                .body(request, MediaType.APPLICATION_JSON)
                .post(AcountTransactionsResponse.class);
    }

    public PositionWalletResponse getPositionsWallet(
            String internalProductCode, String resumePoint) {
        ValueAccountIdentifierEntity accountIdentifier =
                ValueAccountIdentifierEntity.fromInternalProductCode(internalProductCode);

        DataHomeModelEntity dataHomeModel = new DataHomeModelEntity(accountIdentifier, resumePoint);

        PositionWalletRequest request = new PositionWalletRequest(dataHomeModel);

        return client.request(BankiaConstants.Url.VALUE_ACCOUNT_POSITION_WALLET)
                .queryParam(
                        BankiaConstants.Query.CM_FORCED_DEVICE_TYPE, BankiaConstants.Default.JSON)
                .queryParam(BankiaConstants.Query.J_GID_COD_APP, BankiaConstants.Default.O3)
                .queryParam(
                        BankiaConstants.Query.J_GID_COD_DS, BankiaConstants.Default.LOWER_CASE_OIP)
                .queryParam(BankiaConstants.Query.X_J_GID_COD_APP, BankiaConstants.Default.O3)
                .body(request, MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .post(PositionWalletResponse.class);
    }

    public void getDisconnect() {
        client.request(BankiaConstants.Url.DISCONNECT)
                .queryParam(BankiaConstants.Query.ORIGEN, BankiaConstants.Default.UPPER_CASE_AM)
                .queryParam(
                        BankiaConstants.Query.CM_FORCED_DEVICE_TYPE, BankiaConstants.Default.JSON)
                .accept(MediaType.APPLICATION_JSON)
                .get(String.class);
    }

    public LoginResponse initiateLogin() {
        return createRequest(BankiaConstants.Url.LOGIN)
                .queryParam(BankiaConstants.Query.VERSION, BankiaConstants.Default._5_0)
                .queryParam(BankiaConstants.Query.TIPO, BankiaConstants.Default.ANDROID_PHONE)
                .get(LoginResponse.class);
    }

    public RsaKeyResponse getLoginKey() {
        return createRequest(BankiaConstants.Url.LOGIN_KEY).get(RsaKeyResponse.class);
    }

    public LoginResponse login(
            String username, String password, String execution, String encryptionKey) {

        String encryptedPassword = BankiaCrypto.encryptPassword(password, encryptionKey);

        LoginRequest formBody =
                LoginRequest.create(
                        persistentStorage, username, password, execution, encryptedPassword);

        return createRequest(BankiaConstants.Url.LOGIN)
                .queryParam(BankiaConstants.Query.VERSION, BankiaConstants.Default._5_0)
                .queryParam(BankiaConstants.Query.TIPO, BankiaConstants.Default.ANDROID_PHONE)
                .body(formBody, MediaType.APPLICATION_FORM_URLENCODED)
                .post(LoginResponse.class);
    }

    /**
     * This method is the first call made to the "oip.bankia.es" domain and is necessary for any
     * subsequent calls to work (getting accounts and transactions). If the user is not properly
     * logged in it returns status 4XX. If the call is sucessful the response contains some basic
     * info about the client's contracts. If the call fails (4XX) the response contains the same
     * response as {@link #getLoginKey()}. We're not interested in the actual response... Doesn't
     * seem to be a problem to call this method repeatedly.
     *
     * @return true if the user is properly logged in, otherwise false
     */
    public boolean authorizeSession() {
        try {
            createInSessionRequest(BankiaConstants.Url.GLOBAL_POSITION_CLIENT_SCENARIO)
                    .get(String.class);
            return true;
        } catch (HttpResponseException exception) {
            int status = exception.getResponse().getStatus();
            if (400 <= status && status < 500) {
                return false;
            }

            // Re-throw unknown exception.
            throw exception;
        }
    }

    public LoanDetailsResponse getLoanDetails(LoanDetailsRequest loanDetailsRequest) {
        return createInSessionRequest(BankiaConstants.Url.LOAN_DETAILS)
                .body(loanDetailsRequest, MediaType.APPLICATION_JSON)
                .post(LoanDetailsResponse.class);
    }

    public IdentityDataResponse fetchIdentityData() {
        return client.request(BankiaConstants.Url.IDENTITY_DATA)
                .header(BankiaConstants.Query.J_GID_COD_DS, BankiaConstants.Default.LOWER_CASE_OIP)
                .header(BankiaConstants.Query.X_J_GID_COD_APP, BankiaConstants.Default.O3)
                .header(BankiaConstants.Query.J_GID_COD_APP, BankiaConstants.Default.O3)
                .accept(MediaType.APPLICATION_JSON)
                .get(IdentityDataResponse.class);
    }
}
