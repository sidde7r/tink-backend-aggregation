package se.tink.backend.aggregation.agents.brokers.avanza;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.filter.ClientFilter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.PersistentLogin;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.brokers.avanza.AvanzaV2Constants.Headers;
import se.tink.backend.aggregation.agents.brokers.avanza.AvanzaV2Constants.InstrumentTypes;
import se.tink.backend.aggregation.agents.brokers.avanza.AvanzaV2Constants.Payloads;
import se.tink.backend.aggregation.agents.brokers.avanza.AvanzaV2Constants.QueryParams;
import se.tink.backend.aggregation.agents.brokers.avanza.AvanzaV2Constants.Urls;
import se.tink.backend.aggregation.agents.brokers.avanza.v2.model.AccountDetailsEntity;
import se.tink.backend.aggregation.agents.brokers.avanza.v2.model.AccountOverviewEntity;
import se.tink.backend.aggregation.agents.brokers.avanza.v2.model.LoginEntity;
import se.tink.backend.aggregation.agents.brokers.avanza.v2.model.PositionAggregationEntity;
import se.tink.backend.aggregation.agents.brokers.avanza.v2.model.PositionEntity;
import se.tink.backend.aggregation.agents.brokers.avanza.v2.model.Session;
import se.tink.backend.aggregation.agents.brokers.avanza.v2.model.TransactionEntity;
import se.tink.backend.aggregation.agents.brokers.avanza.v2.rpc.BankIdResponse;
import se.tink.backend.aggregation.agents.brokers.avanza.v2.rpc.BondMarketInfoResponse;
import se.tink.backend.aggregation.agents.brokers.avanza.v2.rpc.CertificateMarketInfoResponse;
import se.tink.backend.aggregation.agents.brokers.avanza.v2.rpc.CreateSessionResponse;
import se.tink.backend.aggregation.agents.brokers.avanza.v2.rpc.EquityLinkedBondMarketInfoResponse;
import se.tink.backend.aggregation.agents.brokers.avanza.v2.rpc.ExchangeTradedFundInfoResponse;
import se.tink.backend.aggregation.agents.brokers.avanza.v2.rpc.FundMarketInfoResponse;
import se.tink.backend.aggregation.agents.brokers.avanza.v2.rpc.FutureForwardMarketInfoResponse;
import se.tink.backend.aggregation.agents.brokers.avanza.v2.rpc.InitiateBankIdRequest;
import se.tink.backend.aggregation.agents.brokers.avanza.v2.rpc.InitiateBankIdResponse;
import se.tink.backend.aggregation.agents.brokers.avanza.v2.rpc.InvestmentTransactionsResponse;
import se.tink.backend.aggregation.agents.brokers.avanza.v2.rpc.PositionResponse;
import se.tink.backend.aggregation.agents.brokers.avanza.v2.rpc.StockMarketInfoResponse;
import se.tink.backend.aggregation.agents.brokers.avanza.v2.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.brokers.avanza.v2.rpc.WarrantMarketInfoResponse;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.libraries.credentials_requests.CredentialsRequest;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.models.Transaction;

/** Latest verified version: iOS v2.12.0 */
public class AvanzaV2Agent extends AbstractAgent
        implements RefreshInvestmentAccountsExecutor, PersistentLogin {
    private String authenticationToken;
    private Client client;
    private Credentials credentials;
    private AccountOverviewEntity accountOverview;
    private Session session;

    public AvanzaV2Agent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context);

        credentials = request.getCredentials();
        client = clientFactory.createBasicClient(context.getLogOutputStream());

        // Add a filter that automatically intercepts and adds CSRF tokens.

        addAuthenticationTokenFilter();
    }

    /** Helper method to add client filter that intercepts and adds CSRF tokens. */
    private void addAuthenticationTokenFilter() {
        client.addFilter(
                new ClientFilter() {
                    @Override
                    public ClientResponse handle(ClientRequest cr) throws ClientHandlerException {
                        ClientResponse response = getNext().handle(cr);

                        authenticationToken =
                                Optional.ofNullable(response.getHeaders())
                                        .filter(h -> h.containsKey(Headers.SECURITY_TOKEN))
                                        .map(h -> h.getFirst(Headers.SECURITY_TOKEN))
                                        .orElse(null);

                        return response;
                    }
                });
    }

    private boolean authenticateBankId() throws SessionException, BankIdException {
        // If we're not in a manual refresh, the authentication session is invalid but we can't
        // prompt the user to
        // re-authenticate using BankId.

        if (!request.isManual()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        credentials.setSensitivePayload(Payloads.AUTHENTICATION_SESSION_PAYLOAD, null);

        InitiateBankIdRequest initiateBankIdRequest = new InitiateBankIdRequest();
        initiateBankIdRequest.setIdentificationNumber(credentials.getField(Field.Key.USERNAME));

        InitiateBankIdResponse initiateBankIdResponse =
                createClientRequest(Urls.BANK_ID_INIT)
                        .post(InitiateBankIdResponse.class, initiateBankIdRequest);

        credentials.setSupplementalInformation(null);
        credentials.setStatus(CredentialsStatus.AWAITING_MOBILE_BANKID_AUTHENTICATION);

        supplementalRequester.requestSupplementalInformation(credentials, false);

        Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);

        for (int i = 0; i < 30; i++) {

            ClientResponse bankIdClientResponse =
                    createClientRequest(
                                    String.format(
                                            Urls.BANK_ID_COLLECT,
                                            initiateBankIdResponse.getTransactionId()))
                            .get(ClientResponse.class);

            if (bankIdClientResponse.getStatus() != Status.OK.getStatusCode()) {
                log.warn(
                        "Could not login to BankID. Most likely because user cancelled the request");
                throw BankIdError.CANCELLED.exception();
            }

            BankIdResponse bankIdResponse = bankIdClientResponse.getEntity(BankIdResponse.class);

            switch (bankIdResponse.getState()) {
                case "OUTSTANDING_TRANSACTION":
                case "USER_SIGN":
                case "STARTED":
                    log.debug("Waiting for BankID authentication: " + bankIdResponse.getState());

                    Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);
                    continue;
                case "NO_CLIENT":
                    log.warn("Got erroneous BankID status: " + bankIdResponse.getState());
                    throw BankIdError.NO_CLIENT.exception();
                case "COMPLETE":

                    // Create one or several sessions depending on if the user has multiple accounts
                    this.session = new Session();

                    for (LoginEntity entity : bankIdResponse.getLogins()) {
                        ClientResponse clientResponse =
                                createClientRequest(Urls.HOST + entity.getLoginPath())
                                        .post(ClientResponse.class, request);

                        int status = clientResponse.getStatus();

                        Preconditions.checkState(
                                status == Status.OK.getStatusCode(),
                                "#login-refactoring - Could not login to BankID. Error Status: "
                                        + status);
                        CreateSessionResponse sessionResponse =
                                clientResponse.getEntity(CreateSessionResponse.class);
                        this.session.addAuthenticationSession(
                                sessionResponse.getAuthenticationSession());
                    }

                    return true;
                default:
                    throw new IllegalStateException(
                            "#login-refactoring - Unknown status for BankID authentication: "
                                    + bankIdResponse.getState());
            }
        }

        // This only happens in the case of a timeout.
        throw BankIdError.TIMEOUT.exception();
    }

    private Builder createClientRequest(String url) {
        return createClientRequest(
                url, credentials.getSensitivePayload(Payloads.AUTHENTICATION_SESSION_PAYLOAD));
    }

    private Builder createClientRequest(String url, String authenticationSession) {
        Builder builder =
                client.resource(url)
                        .header("User-Agent", DEFAULT_USER_AGENT)
                        .accept(MediaType.APPLICATION_JSON)
                        .type(MediaType.APPLICATION_JSON);

        if (!Strings.isNullOrEmpty(authenticationSession)) {
            builder = builder.header(Headers.AUTHENTICATION_SESSION, authenticationSession);
        }

        if (!Strings.isNullOrEmpty(authenticationToken)) {
            builder = builder.header(Headers.SECURITY_TOKEN, authenticationToken);
        }

        return builder;
    }

    /** Fetch the account overview, return null if not successful. */
    private AccountOverviewEntity fetchOverview(String authenticationSession) {
        // If we don't have an existing authentication session token, just return.

        if (Strings.isNullOrEmpty(authenticationSession)) {
            return null;
        }

        ClientResponse clientResponse =
                createClientRequest(Urls.ACCOUNTS_OVERVIEW, authenticationSession)
                        .get(ClientResponse.class);

        if (clientResponse.getStatus() == Status.OK.getStatusCode()) {
            return clientResponse.getEntity(AccountOverviewEntity.class);
        } else {
            return null;
        }
    }

    private void ensureValidBankIDSession(String bankIDAuthenticationSession) {
        Preconditions.checkNotNull(bankIDAuthenticationSession);

        accountOverview = fetchOverview(bankIDAuthenticationSession);

        Preconditions.checkNotNull(accountOverview);
        Preconditions.checkNotNull(accountOverview.getAccounts());
    }

    private TransactionsResponse fetchTransactions(
            String accountId, String authenticationSession, String fromDate, String toDate) {
        return createClientRequest(
                        String.format(Urls.ACCOUNT_TRANSACTIONS, accountId, fromDate, toDate),
                        authenticationSession)
                .get(TransactionsResponse.class);
    }

    private AccountDetailsEntity fetchAccountDetails(
            String accountId, String authenticationSession) {
        return createClientRequest(
                        String.format(Urls.ACCOUNT_DETAILS, accountId), authenticationSession)
                .get(AccountDetailsEntity.class);
    }

    private InvestmentTransactionsResponse fetchInvestmentTransactions(
            String accountId, String toDate, String authenticationSession) {
        return createClientRequest(
                        String.format(
                                Urls.INVESTMENT_TRANSACTIONS,
                                accountId,
                                QueryParams.FROM_DATE_FOR_INVESTMENT_TRANSACTIONS,
                                toDate),
                        authenticationSession)
                .get(InvestmentTransactionsResponse.class);
    }

    private PositionResponse fetchInvestmentsPositions(
            String accountId, String authenticationSession) {
        return createClientRequest(
                        String.format(Urls.INVESTMENT_POSITIONS, accountId), authenticationSession)
                .get(PositionResponse.class);
    }

    private <T> T getMarketInfoResponse(
            String positionType,
            String orderbookId,
            String authenticationSession,
            Class<T> responseType) {
        return createClientRequest(
                        String.format(Urls.MARKET_INFO, positionType, orderbookId),
                        authenticationSession)
                .get(responseType);
    }

    private Consumer<PositionAggregationEntity> aggregatePositionEntityWithInstrumentType(
            String authenticationSession,
            Map<String, String> isinByName,
            List<Instrument> instruments) {
        return positionAggregationEntity -> {
            String instrumentType = positionAggregationEntity.getInstrumentType();
            positionAggregationEntity
                    .getPositions()
                    .stream()
                    .filter(positionEntityHasOrderbookId())
                    .forEach(
                            mapPositionEntityToTinkInstrument(
                                    authenticationSession,
                                    isinByName,
                                    instruments,
                                    instrumentType));
        };
    }

    private Predicate<PositionEntity> positionEntityHasOrderbookId() {
        return positionEntity -> Objects.nonNull(positionEntity.getOrderbookId());
    }

    private Consumer<PositionEntity> mapPositionEntityToTinkInstrument(
            String authenticationSession,
            Map<String, String> isinByName,
            List<Instrument> instruments,
            String instrumentType) {
        return positionEntity -> {
            String market =
                    getInstrumentMarket(
                            instrumentType, positionEntity.getOrderbookId(), authenticationSession);
            positionEntity
                    .toInstrument(instrumentType, market, isinByName.get(positionEntity.getName()))
                    .ifPresent(instruments::add);
        };
    }

    private List<Transaction> getTransactions(
            Account account, String accountId, String authenticationSession) {
        // Fetch transactions for the account and create Tink Transactions
        int subsequentEmptyFetches = 0;
        int earlierSize = 0;
        List<Transaction> transactions = Lists.newArrayList();
        LocalDate fromDate = LocalDate.now().minusMonths(3);
        LocalDate toDate = LocalDate.now();
        do {
            TransactionsResponse transactionsResponse =
                    fetchTransactions(
                            accountId,
                            authenticationSession,
                            fromDate.format(DateTimeFormatter.ISO_DATE),
                            toDate.format(DateTimeFormatter.ISO_DATE));

            transactions.addAll(
                    transactionsResponse
                            .getTransactions()
                            .stream()
                            .filter(transactionIsDepositOrWithdraw())
                            .map(TransactionEntity::toTinkTransaction)
                            .collect(Collectors.toList()));

            if (transactions.size() == earlierSize) {
                subsequentEmptyFetches++;
            } else {
                subsequentEmptyFetches = 0;
            }

            toDate = fromDate.minusDays(1);
            fromDate = toDate.minusMonths(3);
            earlierSize = transactions.size();
        } while (subsequentEmptyFetches < 3 && !isContentWithRefresh(account, transactions));

        return transactions;
    }

    private Predicate<TransactionEntity> transactionIsDepositOrWithdraw() {
        return t ->
                Objects.equals(t.getTransactionType().toLowerCase(), "deposit")
                        || Objects.equals(t.getTransactionType().toLowerCase(), "withdraw");
    }

    private String getInstrumentMarket(
            String positionType, String orderbookId, String authenticationSession) {
        if (positionType == null) {
            return null;
        }

        String type = positionType.toLowerCase();
        switch (type) {
            case InstrumentTypes.AUTO_PORTFOLIO:
            case InstrumentTypes.FUND:
                FundMarketInfoResponse fundMarketInfoResponse =
                        getMarketInfoResponse(
                                InstrumentTypes.FUND,
                                orderbookId,
                                authenticationSession,
                                FundMarketInfoResponse.class);
                return fundMarketInfoResponse.getFundCompany() != null
                        ? fundMarketInfoResponse.getFundCompany().getName()
                        : null;
            case InstrumentTypes.STOCK:
                return getMarketInfoResponse(
                                type,
                                orderbookId,
                                authenticationSession,
                                StockMarketInfoResponse.class)
                        .getMarketPlace();
            case InstrumentTypes.CERTIFICATE:
                return getMarketInfoResponse(
                                type,
                                orderbookId,
                                authenticationSession,
                                CertificateMarketInfoResponse.class)
                        .getMarketPlace();
            case InstrumentTypes.FUTURE_FORWARD:
                return getMarketInfoResponse(
                                type,
                                orderbookId,
                                authenticationSession,
                                FutureForwardMarketInfoResponse.class)
                        .getMarketPlace();
            case InstrumentTypes.EQUITY_LINKED_BOND:
                return getMarketInfoResponse(
                                type,
                                orderbookId,
                                authenticationSession,
                                EquityLinkedBondMarketInfoResponse.class)
                        .getMarketPlace();
            case InstrumentTypes.BOND:
                return getMarketInfoResponse(
                                type,
                                orderbookId,
                                authenticationSession,
                                BondMarketInfoResponse.class)
                        .getMarketPlace();
            case InstrumentTypes.WARRANT:
                return getMarketInfoResponse(
                                type,
                                orderbookId,
                                authenticationSession,
                                WarrantMarketInfoResponse.class)
                        .getMarketPlace();
            case InstrumentTypes.EXCHANGE_TRADED_FUND:
                return getMarketInfoResponse(
                                type,
                                orderbookId,
                                authenticationSession,
                                ExchangeTradedFundInfoResponse.class)
                        .getMarketPlace();

            default:
                log.warn(
                        String.format(
                                "avanza - portfolio type not handled in switch - type: %s, orderbookId: %s",
                                positionType, orderbookId));
                return null;
        }
    }

    @Override
    public boolean login() throws AuthenticationException, AuthorizationException {
        // Authenticate the user if the session isn't valid.
        if (accountOverview != null) {
            return true;
        } else {
            if (request.getCredentials().getType() == CredentialsTypes.MOBILE_BANKID) {
                return authenticateBankId();
            } else {
                String msg =
                        String.format(
                                "Credential type %s is not supported",
                                request.getCredentials().getType().name());
                throw new IllegalStateException(msg);
            }
        }
    }

    @Override
    public void logout() throws Exception {
        // Implement.
    }

    @Override
    public boolean isLoggedIn() throws Exception {
        return keepAlive();
    }

    /**
     * Keep alive the agent by doing requests to fetch the overview. Need to keep all sessions alive
     * for the connection to be alive
     */
    @Override
    public boolean keepAlive() throws Exception {
        if (session == null || session.isEmpty()) {
            return false;
        }

        int numberOfSessions = session.getAuthenticationSessions().size();

        for (int i = 0; i < numberOfSessions; i++) {
            String authenticationSession = session.getAuthenticationSessions().get(i);
            authenticationToken = null;

            boolean loggedOut = fetchOverview(authenticationSession) == null;

            log.info(String.format("Keeping alive session %s of %s.", i + 1, numberOfSessions));

            if (loggedOut) {
                return false;
            }
        }

        // All session are still alive
        log.info("All sessions are alive");
        return true;
    }

    @Override
    public void persistLoginSession() {
        if (session == null) {
            return;
        }

        credentials.setPersistentSession(session);
    }

    @Override
    public void loadLoginSession() {
        session = credentials.getPersistentSession(Session.class);
    }

    @Override
    public void clearLoginSession() {
        session = null;

        credentials.removePersistentSession();
    }

    ////// Refresh Executor Refactor //////

    @Override
    public FetchInvestmentAccountsResponse fetchInvestmentAccounts() {
        return fetchAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchInvestmentTransactions() {
        return fetchTransactions();
    }

    private FetchInvestmentAccountsResponse fetchAccounts() {
        Map<Account, AccountFeatures> accounts = new HashMap<>();
        if (request.getCredentials().getType() == CredentialsTypes.MOBILE_BANKID) {
            session.getAuthenticationSessions()
                    .forEach(
                            bankIDAuthenticationSession -> {
                                ensureValidBankIDSession(bankIDAuthenticationSession);
                                accountOverview
                                    .getAccounts()
                                    .forEach(
                                        accountEntity -> {
                                            // Only tradable accounts have instruments
                                            if (!accountEntity.isTradable()) {
                                                return;
                                            }

                                            String accountId = accountEntity.getAccountId();

                                            AccountDetailsEntity accountDetailsEntity =
                                                    fetchAccountDetails(accountId, bankIDAuthenticationSession);
                                            Account account = accountDetailsEntity.toAccount(accountEntity);

                                            PositionResponse positionResponse =
                                                    fetchInvestmentsPositions(accountId, bankIDAuthenticationSession);
                                            InvestmentTransactionsResponse investmentTransactionsResponse =
                                                    fetchInvestmentTransactions(
                                                            accountId,
                                                            LocalDate.now().format(DateTimeFormatter.ISO_DATE),
                                                            bankIDAuthenticationSession);

                                            Map<String, String> isinByName =
                                                    investmentTransactionsResponse.getIsinByName();

                                            Portfolio portfolio = positionResponse.toPortfolio();
                                            // Add the money available for buying instruments
                                            portfolio.setCashValue(accountDetailsEntity.getBuyingPower());
                                            List<Instrument> instruments = Lists.newArrayList();
                                            positionResponse
                                                    .getInstrumentPositions()
                                                    .forEach(
                                                            aggregatePositionEntityWithInstrumentType(
                                                                    bankIDAuthenticationSession,
                                                                    isinByName,
                                                                    instruments));
                                            portfolio.setInstruments(instruments);

                                            account.setBalance(
                                                    account.getBalance() + accountDetailsEntity.getBuyingPower());

                                            accounts.put(
                                                    account, AccountFeatures.createForPortfolios(portfolio));
                                        });
                            });
            return new FetchInvestmentAccountsResponse(accounts);
        } else {
            log.error(
                    String.format(
                            "Credential type %s is not supported",
                            request.getCredentials().getType().name()));
            return new FetchInvestmentAccountsResponse(Collections.emptyMap());
        }
    }
    private FetchTransactionsResponse fetchTransactions() {
        Map<Account, List<Transaction>> transactionsMap = new HashMap<>();
        if (request.getCredentials().getType() == CredentialsTypes.MOBILE_BANKID) {
            session.getAuthenticationSessions()
                    .forEach(
                            bankIDAuthenticationSession -> {
                                ensureValidBankIDSession(bankIDAuthenticationSession);
                                accountOverview
                                        .getAccounts()
                                        .forEach(
                                                accountEntity -> {
                                                    String accountId = accountEntity.getAccountId();

                                                    AccountDetailsEntity accountDetailsEntity =
                                                            fetchAccountDetails(accountId, bankIDAuthenticationSession);
                                                    Account account = accountDetailsEntity.toAccount(accountEntity);

                                                    // Hack to get the correct name for SparkontoPlus accounts.
                                                    if (Objects.equals(
                                                            accountDetailsEntity.getAccountType(), "SparkontoPlus")) {
                                                        account.setName(
                                                                accountDetailsEntity.getAccountTypeName()
                                                                        + accountEntity.getSparkontoPlusType());
                                                    }

                                                    List<Transaction> transactions =
                                                            getTransactions(account, accountId, bankIDAuthenticationSession);

                                                    transactionsMap.put(account, transactions);
                                                });
                            });
            return new FetchTransactionsResponse(transactionsMap);
        } else {
            log.error(
                    String.format(
                            "Credential type %s is not supported",
                            request.getCredentials().getType().name()));
            return new FetchTransactionsResponse(Collections.emptyMap());
        }
    }
    ///////////////////////////////////////
}
