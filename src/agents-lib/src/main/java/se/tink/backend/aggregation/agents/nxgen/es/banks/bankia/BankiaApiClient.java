package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia;

import java.time.LocalDate;
import java.util.List;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.authenticator.rpc.RsaKeyResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities.DateEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities.AccountIdentifierEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities.SearchCriteriaEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.rpc.AccountTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.rpc.AcountTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.rpc.ContractsResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class BankiaApiClient {

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

    private ContractsResponse getServicesContracts() {
        return client.request(BankiaConstants.Url.SERVICES_CONTRACTS)
                .queryParam(BankiaConstants.Query.GROUP_BY_FAMILIA, BankiaConstants.Default.TRUE)
                .queryParam(BankiaConstants.Query.ID_VISTA, BankiaConstants.Default._1)
                .queryParam(BankiaConstants.Query.J_GID_COD_APP, BankiaConstants.Default.O3)
                .queryParam(BankiaConstants.Query.J_GID_COD_DS, BankiaConstants.Default.LOWER_CASE_OIP)
                .queryParam(BankiaConstants.Query.X_J_GID_COD_APP, BankiaConstants.Default.LOWER_CASE_AM)
                .queryParam(BankiaConstants.Query.CM_FORCED_DEVICE_TYPE, BankiaConstants.Default.JSON)
                .accept(MediaType.APPLICATION_JSON)
                .get(ContractsResponse.class);
    }

    public AcountTransactionsResponse getTransactions(Account account, LocalDate fromDate, LocalDate toDate) {
        AccountIdentifierEntity accountIdentifier = new AccountIdentifierEntity();
        accountIdentifier.setCountry(account.getTemporaryStorage(BankiaConstants.StorageKey.COUNTRY, String.class));
        accountIdentifier.setControlDigits(account.getTemporaryStorage(BankiaConstants.StorageKey.CONTROL_DIGITS,
                String.class));
        accountIdentifier.setIdentifier(account.getBankIdentifier());

        SearchCriteriaEntity searchCriteria = new SearchCriteriaEntity();
        searchCriteria.setDateOperationFrom(DateEntity.of(fromDate));
        searchCriteria.setOperationDateUntil(DateEntity.of(toDate));

        AccountTransactionsRequest request = AccountTransactionsRequest.create(accountIdentifier, searchCriteria,
                BankiaConstants.LANGUAGE);

        return client.request(BankiaConstants.Url.SERVICES_ACCOUNT_MOVEMENT)
                .queryParam(BankiaConstants.Query.J_GID_COD_APP, BankiaConstants.Default.O3)
                .queryParam(BankiaConstants.Query.J_GID_COD_DS, BankiaConstants.Default.LOWER_CASE_OIP)
                .queryParam(BankiaConstants.Query.X_J_GID_COD_APP, BankiaConstants.Default.LOWER_CASE_AM)
                .queryParam(BankiaConstants.Query.CM_FORCED_DEVICE_TYPE, BankiaConstants.Default.JSON)
                .body(request, MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .post(AcountTransactionsResponse.class);
    }

    public void getDisconnect() {
        client.request(BankiaConstants.Url.DISCONNECT)
                .queryParam(BankiaConstants.Query.ORIGEN, BankiaConstants.Default.UPPER_CASE_AM)
                .queryParam(BankiaConstants.Query.CM_FORCED_DEVICE_TYPE, BankiaConstants.Default.JSON)
                .accept(MediaType.APPLICATION_JSON)
                .get(String.class);
    }

    public LoginResponse initiateLogin() {
        return client.request(BankiaConstants.Url.LOGIN)
                .queryParam(BankiaConstants.Query.CM_FORCED_DEVICE_TYPE, BankiaConstants.Default.JSON)
                .queryParam(BankiaConstants.Query.VERSION, BankiaConstants.Default._5_0)
                .queryParam(BankiaConstants.Query.TIPO, BankiaConstants.Default.ANDROID_PHONE)
                .queryParam(BankiaConstants.Query.OIGID, BankiaConstants.Default.TRUE)
                .queryParam(BankiaConstants.Query.J_GID_COD_APP, BankiaConstants.Default.LOWER_CASE_AM)
                .queryParam(BankiaConstants.Query.J_GID_COD_DS, BankiaConstants.Default.UPPER_CASE_OIP)
                .queryParam(BankiaConstants.Query.ORIGEN, BankiaConstants.Default.UPPER_CASE_AM)
                .accept(MediaType.APPLICATION_JSON)
                .get(LoginResponse.class);
    }

    public RsaKeyResponse getLoginKey() {
        return client.request(BankiaConstants.Url.LOGIN_KEY)
                .queryParam(BankiaConstants.Query.CM_FORCED_DEVICE_TYPE, BankiaConstants.Default.JSON)
                .queryParam(BankiaConstants.Query.OIGID, BankiaConstants.Default.TRUE)
                .queryParam(BankiaConstants.Query.J_GID_COD_APP, BankiaConstants.Default.LOWER_CASE_AM)
                .queryParam(BankiaConstants.Query.J_GID_COD_DS, BankiaConstants.Default.UPPER_CASE_OIP)
                .queryParam(BankiaConstants.Query.ORIGEN, BankiaConstants.Default.UPPER_CASE_AM)
                .accept(MediaType.APPLICATION_JSON)
                .get(RsaKeyResponse.class);
    }

    public LoginResponse login(String username, String password, String execution, String encryptionKey) {

        String encryptedPassword = BankiaCrypto.encryptPassword(password, encryptionKey);

        LoginRequest formBody = LoginRequest.create(persistentStorage, username, password, execution,
                encryptedPassword);

        return client.request(BankiaConstants.Url.LOGIN)
                .queryParam(BankiaConstants.Query.CM_FORCED_DEVICE_TYPE, BankiaConstants.Default.JSON)
                .queryParam(BankiaConstants.Query.VERSION, BankiaConstants.Default._5_0)
                .queryParam(BankiaConstants.Query.TIPO, BankiaConstants.Default.ANDROID_PHONE)
                .queryParam(BankiaConstants.Query.OIGID, BankiaConstants.Default.TRUE)
                .queryParam(BankiaConstants.Query.J_GID_COD_APP, BankiaConstants.Default.LOWER_CASE_AM)
                .queryParam(BankiaConstants.Query.J_GID_COD_DS, BankiaConstants.Default.UPPER_CASE_OIP)
                .queryParam(BankiaConstants.Query.ORIGEN, BankiaConstants.Default.UPPER_CASE_AM)
                .body(formBody, MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .post(LoginResponse.class);
    }

    /**
     * This method is the first call made to the "oip.bankia.es" domain and is necessary for any subsequent calls to work
     * (getting accounts and transactions).
     * If the user is not properly logged in it returns status 4XX.
     * If the call is sucessful the response contains some basic info about the client's contracts.
     * If the call fails (4XX) the response contains the same response as {@link #getLoginKey()}.
     * We're not interested in the actual response...
     * Doesn't seem to be a problem to call this method repeatedly.
     *
     * @return true if the user is properly logged in, otherwise false
     */
    public boolean authorizeSession() {
        try {
            client.request(BankiaConstants.Url.GLOBAL_POSITION_CLIENT_SCENARIO)
                    .queryParam(BankiaConstants.Query.J_GID_COD_APP, BankiaConstants.Default.O3)
                    .queryParam(BankiaConstants.Query.J_GID_COD_DS, BankiaConstants.Default.LOWER_CASE_OIP)
                    .queryParam(BankiaConstants.Query.X_J_GID_COD_APP, BankiaConstants.Default.LOWER_CASE_AM)
                    .queryParam(BankiaConstants.Query.CM_FORCED_DEVICE_TYPE, BankiaConstants.Default.JSON)
                    .accept(MediaType.APPLICATION_JSON)
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
}
