package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia;

import com.google.api.client.util.Strings;
import io.vavr.control.Either;
import io.vavr.control.Try;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.RequestFactory.Scope;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.authenticator.BankiaCrypto;
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
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.rpc.LoanDetailsErrorCode;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.rpc.LoanDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.rpc.ContractsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities.AccountIdentifierEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities.PaginationDataEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities.SearchCriteriaEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.rpc.AccountDetailsErrorCode;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.rpc.AccountDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.rpc.AccountTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.rpc.AcountTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BankiaApiClient {
    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final RequestFactory requestFactory;

    public BankiaApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            RequestFactory requestFactory) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.requestFactory = requestFactory;
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

    public List<LoanAccountEntity> getLoans() {
        return getServicesContracts().getLoans();
    }

    public CardTransactionsResponse getCardTransactions(CardTransactionsRequest request) {
        return requestFactory
                .create(Scope.WITH_SESSION, URL.of(BankiaConstants.Url.CREDIT_CARD_TRANSACTIONS))
                .body(request, MediaType.APPLICATION_JSON)
                .post(CardTransactionsResponse.class);
    }

    private ContractsResponse getServicesContracts() {
        try {
            return requestFactory
                    .create(Scope.WITH_SESSION, URL.of(BankiaConstants.Url.SERVICES_CONTRACTS))
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

        return requestFactory
                .create(Scope.WITH_SESSION, URL.of(BankiaConstants.Url.SERVICES_ACCOUNT_MOVEMENT))
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

    public RsaKeyResponse getLoginKey() {
        try {
            return requestFactory
                    .create(Scope.WITHOUT_SESSION, URL.of(BankiaConstants.Url.LOGIN_KEY))
                    .get(RsaKeyResponse.class);
        } catch (HttpResponseException exception) {
            if (exception.getResponse().getStatus() == HttpStatus.SC_FORBIDDEN) {
                return exception.getResponse().getBody(RsaKeyResponse.class);
            }
            throw exception;
        }
    }

    public LoginResponse login(
            String username, String password, String execution, String encryptionKey) {

        String encryptedPassword = BankiaCrypto.encryptPassword(password, encryptionKey);

        LoginRequest formBody =
                LoginRequest.create(
                        persistentStorage, username, password, execution, encryptedPassword);
        try {
            return requestFactory
                    .create(Scope.WITHOUT_SESSION, URL.of(BankiaConstants.Url.LOGIN))
                    .queryParam(
                            BankiaConstants.Query.X_J_GID_COD_APP,
                            BankiaConstants.Default.LOWER_CASE_AM)
                    .body(formBody, MediaType.APPLICATION_FORM_URLENCODED)
                    .post(LoginResponse.class);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_SERVICE_UNAVAILABLE
                    || e.getResponse().getStatus() == BankiaConstants.Errors.LOGIN_ERROR) {
                return e.getResponse().getBody(LoginResponse.class);
            }
            throw e;
        }
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
            requestFactory
                    .create(Scope.WITH_SESSION, URL.of(BankiaConstants.Url.CUSTOMER_SCENARIO))
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

    public Either<LoanDetailsErrorCode, LoanDetailsResponse> getLoanDetails(
            LoanDetailsRequest request) {
        return Try.of(
                        () ->
                                requestFactory
                                        .create(Scope.WITH_SESSION, request.getURL())
                                        .body(request, MediaType.APPLICATION_JSON)
                                        .post(LoanDetailsResponse.class))
                .toEither()
                .mapLeft(LoanDetailsErrorCode::getErrorCode);
    }

    public Either<AccountDetailsErrorCode, AccountDetailsResponse> getAccountDetails(
            AccountDetailsRequest request) {
        return Try.of(
                        () ->
                                requestFactory
                                        .create(Scope.WITH_SESSION, request.getURL())
                                        .body(request, MediaType.APPLICATION_JSON)
                                        .post(AccountDetailsResponse.class))
                .toEither()
                .mapLeft(AccountDetailsErrorCode::getErrorCode);
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
