package se.tink.backend.aggregation.agents.banks.se.collector;

import java.util.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.tink.backend.aggregation.agents.banks.se.collector.models.AccountEntities;
import se.tink.backend.aggregation.agents.banks.se.collector.models.AccountEntity;
import se.tink.backend.aggregation.agents.banks.se.collector.models.CollectAuthenticationResponse;
import se.tink.backend.aggregation.agents.banks.se.collector.models.CollectBankIdResponse;
import se.tink.backend.aggregation.agents.banks.se.collector.models.InitBankIdRequest;
import se.tink.backend.aggregation.agents.banks.se.collector.models.InitBankIdResponse;
import se.tink.backend.aggregation.agents.banks.se.collector.models.InitSignRequest;
import se.tink.backend.aggregation.agents.banks.se.collector.models.TransactionsResponse;
import se.tink.backend.aggregation.agents.banks.se.collector.models.WithdrawalRequest;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.agents.rpc.Account;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.date.ThreadSafeDateFormat;

class CollectorApiClient {
    private static final AggregationLogger log = new AggregationLogger(CollectorApiClient.class);

    /*
        ENV explanation:

        test: using real identities (prod bankid) but test env meaning
        fake accounts, agreements and money

        prod: using real identities (prod bankid) and prod env meaning
        real accounts, agreements and money
    */

    private static final String ENV = "prod";
//    private static final String ENV = "test";

    private static final String BASE_URL = "https://api.collector.se";

    private static final String BANKID_URI = String.format("/mobiltbankid-%s/v3", ENV);
    private static final String BANKID_AUTH_URI = BANKID_URI + "/auth";
    private static final String BANKID_SIGN_URI = BANKID_URI + "/sign";
    private static final String COLLECT_AUTH_URI = BANKID_AUTH_URI + "/%s";
    private static final String COLLECT_SIGN_URI = BANKID_SIGN_URI + "/%s";
    private static final String REFRESH_ACCESS_URI = BANKID_AUTH_URI + "/refresh/%s";

    private static final String BRIDGE_URI = String.format("/tink-bridge-%s", ENV);
    private static final String ACCOUNTS_URI = BRIDGE_URI + "/v1/accounts";
    private static final String TRANSACTIONS_URI = ACCOUNTS_URI + "/%s/history";
    private static final String WITHDRAWAL_URI = ACCOUNTS_URI + "/%s/withdrawal";

    private static final String SIGN_TEXT_FORMAT_COLLECTOR_SAVE = "Jag bekräftar öppnandet av sparkonto Collector Save %s och godkänner de allmänna villkoren för sparkontot och Collector Banks behandling av personuppgifter. Jag bekräftar även att jag mottagit information om statlig insättningsgaranti.";

    private final Client client;
    private String subscriptionKey;
    private String accessToken;
    private final String userAgent;

    private AccountEntities accounts = new AccountEntities();

    CollectorApiClient(Client client, String userAgent) {
        this.client = client;
        this.userAgent = userAgent;
    }

    void setSubscriptionKey(String subscriptionKey) {
        this.subscriptionKey = subscriptionKey;
    }

    String initBankId(BankIdOperationType operationType, String username) {
        InitBankIdRequest request;
        String uri = BankIdOperationType.AUTH.equals(operationType) ? BANKID_AUTH_URI : BANKID_SIGN_URI;

        if (BankIdOperationType.AUTH.equals(operationType)) {
            request = new InitBankIdRequest(username);
        } else {
            String message = String.format(SIGN_TEXT_FORMAT_COLLECTOR_SAVE,
                    ThreadSafeDateFormat.FORMATTER_DAILY.format(new Date()));
            request = new InitSignRequest(username, message);
        }

        String sessionId = post(uri, request, InitBankIdResponse.class).getSessionId();

        Preconditions.checkState(!Strings.isNullOrEmpty(sessionId),
                String.format("Didn't get sessionId when initiating BankID (operationType=%s)", operationType));

        return sessionId;
    }

    boolean isAlive(String accessToken) {
        if (Strings.isNullOrEmpty(accessToken)) {
            log.info("AccessToken not available");
            return false;
        }

        try {
            this.accessToken = accessToken;
            fetchAccounts();

            return true;
        } catch (UniformInterfaceException e) {
            return false;
        }
    }

    CollectAuthenticationResponse refreshAccessToken(String refreshToken) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(refreshToken),
                "RefreshToken not available");

        return get(String.format(REFRESH_ACCESS_URI, refreshToken), CollectAuthenticationResponse.class);
    }

    <T extends CollectBankIdResponse> Optional<T> collectBankId(String sessionId, Class<T> responseEntity)
            throws BankIdException {
        String uri = Objects.equals(responseEntity, CollectAuthenticationResponse.class)
                ? COLLECT_AUTH_URI : COLLECT_SIGN_URI;

        T response = get(String.format(uri, sessionId), responseEntity);

        log.info(String.format("BankID authentication in progress, status=%s", response.getStatus()));

        switch (response.getStatus()) {
            case DONE:
                Preconditions.checkState(response.isValid(),
                        String.format("Invalid CollectBankIdResponse: %s", response));

                return Optional.of(response);
            case WAITING:
                return Optional.empty();
            case CANCELLED:
                throw BankIdError.CANCELLED.exception();
            case TIMEOUT:
                throw BankIdError.TIMEOUT.exception();
            default:
                throw new IllegalStateException("Unknown error detected while collecting BankID status: " + response);
        }
    }

    public List<Account> getAccounts() {
        if (accounts.isEmpty()) {
            fetchAccounts();
        }

        return accounts.toTinkAccounts();
    }

    private void fetchAccounts() {
        ClientResponse response = get(ACCOUNTS_URI, ClientResponse.class);

        if (Objects.equals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode())) {
            accounts = new AccountEntities();
        } else if (Objects.equals(response.getStatus(), Response.Status.OK.getStatusCode())) {
            accounts = response.getEntity(AccountEntities.class);
        } else {
            throw new UniformInterfaceException(response);
        }
    }

    SwedishIdentifier getWithdrawalIdentifierFor(Account account) {
        AccountEntity accountEntity = accounts.find(account.getBankId());
        if (accountEntity == null) {
            log.error(String.format("accountEntity is null, bankId: %s", account.getBankId()));
            return null;
        }

        AccountIdentifier identifier = accountEntity.getWithdrawalIdentifier();
        if (!identifier.isValid()) {
            log.error(String.format("identifier is not valid: %s", accountEntity.getWithdrawalAccount()));
            return null;
        }

        return identifier.to(SwedishIdentifier.class);
    }

    List<Transaction> fetchTransactionsFor(Account account) {
        TransactionsResponse response = get(String.format(TRANSACTIONS_URI, account.getBankId()), TransactionsResponse.class);

        return response.toTinkTransactions();
    }

    void makeWithdrawal(Transfer transfer, Account account) {
        ClientResponse response = post(String.format(WITHDRAWAL_URI, account.getBankId()),
                WithdrawalRequest.from(transfer), ClientResponse.class);

        if (!Objects.equals(response.getStatus(), ClientResponse.Status.CREATED.getStatusCode())) {
            throw new UniformInterfaceException(response);
        }
    }

    private <R> R get(String url, Class<R> responseClass) {
        return createClientRequest(url).get(responseClass);
    }

    private <R> R post(String url, Object requestData, Class<R> responseClass) {
        return createClientRequest(url).post(responseClass, requestData);
    }

    private WebResource.Builder createClientRequest(String uri) {
        WebResource.Builder builder = client.resource(BASE_URL + uri)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .header("Ocp-Apim-Subscription-Key", subscriptionKey)
                .header("User-Agent", userAgent);

        if (!Strings.isNullOrEmpty(accessToken)) {
            builder.header("Authorization", String.format("Bearer %s", accessToken));
        }

        return builder;
    }

    void rememberAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    void clearAccountsCache() {
        accounts = new AccountEntities();
    }
}
