package se.tink.backend.aggregation.agents.banks.norwegian;

import com.google.api.client.http.HttpStatusCodes;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.MediaType;
import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.DeprecatedRefreshExecutor;
import se.tink.backend.aggregation.agents.banks.norwegian.model.AccountEntity;
import se.tink.backend.aggregation.agents.banks.norwegian.model.CollectBankIdRequest;
import se.tink.backend.aggregation.agents.banks.norwegian.model.CollectBankIdResponse;
import se.tink.backend.aggregation.agents.banks.norwegian.model.CreditCardInfoResponse;
import se.tink.backend.aggregation.agents.banks.norwegian.model.ErrorEntity;
import se.tink.backend.aggregation.agents.banks.norwegian.model.LoginRequest;
import se.tink.backend.aggregation.agents.banks.norwegian.model.OrderBankIdResponse;
import se.tink.backend.aggregation.agents.banks.norwegian.model.TransactionEntity;
import se.tink.backend.aggregation.agents.banks.norwegian.model.TransactionListResponse;
import se.tink.backend.aggregation.agents.banks.norwegian.utils.CreditCardParsingUtils;
import se.tink.backend.aggregation.agents.banks.norwegian.utils.CreditCardParsingUtils.AccountNotFoundException;
import se.tink.backend.aggregation.agents.banks.norwegian.utils.SavingsAccountParsingUtils;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.utils.jsoup.ElementUtils;
import se.tink.backend.aggregation.agents.utils.signicat.SignicatParsingUtils;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

/**
 * Agent will import data from Bank Norwegian. It is only possible to have one credit-card per
 * person at Norwegian so the only Tink account that will be created right now is a credit-card
 * account. No support for Loans.
 */
public class NorwegianAgent extends AbstractAgent implements DeprecatedRefreshExecutor {

    private static final int AUTHENTICATION_BANK_ID_RETRIES = 60;

    private static final String BASE_URL = "https://www.banknorwegian.se/";
    private static final String CREDIT_CARD_URL = BASE_URL + "MinSida/Creditcard/";
    private static final String SAVINGS_ACCOUNTS_URL = BASE_URL + "MinSida/SavingsAccount";
    private static final String CARD_TRANSACTION_URL = CREDIT_CARD_URL + "Transactions";

    private static final int PAGINATION_MONTH_STEP = 3;
    private static final Date PAGINATION_LIMIT = new GregorianCalendar(2012, 1, 1).getTime();

    private static class QueryKeys {
        private static final String ACCOUNT_NUMBER = "accountNo";
        private static final String DATE_FROM = "dateFrom";
        private static final String DATE_TO = "dateTo";
        private static final String GET_LAST_DAYS = "getLastDays";
        private static final String FROM_LAST_EOC = "fromLastEOC";
        private static final String CORE_DOWN = "coreDown";
    }

    private static class QueryValues {
        private static final String GET_LAST_DAYS = "false";
        private static final String FROM_LAST_EOC_FALSE = "false";
        private static final String FROM_LAST_EOC_TRUE = "true";
        private static final String CORE_DOWN = "false";
    }

    private static final URL TRANSACTIONS_PAGINATION_URL =
            new URL(BASE_URL + "MyPage2/Transaction/GetTransactionsFromTo");

    private static final String LOGIN_URL =
            "https://id.banknorwegian.se/std/method/"
                    + "banknorwegian.se/?id=sbid-mobil-2014:default:sv&target=https%3a%2f%2fwww.banknorwegian.se%"
                    + "2fLogin%2fSignicatCallback%3fipid%3d22%26returnUrl%3d%252FMinSida";
    private static final String COLLECT = "collect";
    private static final String ORDER = "order";

    private final Client client;
    private boolean hasRefreshed = false;

    public NorwegianAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context);

        //        DefaultApacheHttpClient4Config config = new DefaultApacheHttpClient4Config();
        //        config.getProperties().put(
        //                ApacheHttpClient4Config.PROPERTY_PROXY_URI,
        //                "http://127.0.0.1:8888"
        //        );
        //        client = clientFactory.createProxyClient(context.getLogOutputStream(), config);

        client = clientFactory.createCookieClient(context.getLogOutputStream());
    }

    @Override
    public boolean login() throws AuthenticationException, AuthorizationException {
        return authenticate(request.getCredentials());
    }

    @Override
    public void refresh() throws Exception {
        // The refresh command will call refresh multiple times.
        // This check ensures the refresh only runs once.
        if (hasRefreshed) {
            return;
        }
        hasRefreshed = true;

        refreshCreditCardAccount();
        refreshSavingsAccount();
    }

    private void refreshCreditCardAccount() throws Exception {
        // Get card and transactions
        try {
            Account account = getAccount();
            updateTransactions(account);
        } catch (AccountNotFoundException e) {
            log.warn("Could not find any creditcard.");
        }
    }

    private void refreshSavingsAccount() throws Exception {
        Optional<Account> savingsAccount = getSavingsAccount();

        if (!savingsAccount.isPresent()) {
            return;
        }

        Account account = savingsAccount.get();
        pageTransactions(account, account.getAccountNumber());
    }

    private boolean authenticate(Credentials credentials) throws BankIdException {

        String initStartPage = client.resource(LOGIN_URL).get(String.class);

        String bankIdUrl = SignicatParsingUtils.parseBankIdServiceUrl(initStartPage);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setSubject(credentials.getField(Field.Key.USERNAME));

        OrderBankIdResponse orderBankIdResponse =
                client.resource(bankIdUrl + ORDER)
                        .type(MediaType.APPLICATION_JSON)
                        .post(OrderBankIdResponse.class, loginRequest);

        if (orderBankIdResponse.getError() != null) {
            if (orderBankIdResponse.getError().isBankIdAlreadyInProgress()) {
                throw BankIdError.ALREADY_IN_PROGRESS.exception();
            }

            throw new IllegalStateException(
                    String.format(
                            "#login-refactoring - Norwegian - Got with code: %s Message: %s",
                            orderBankIdResponse.getError().getCode(),
                            orderBankIdResponse.getError().getMessage()));
        }

        // Prompt a BankId authentication client-side.

        credentials.setSupplementalInformation(null);
        credentials.setStatus(CredentialsStatus.AWAITING_MOBILE_BANKID_AUTHENTICATION);

        supplementalRequester.requestSupplementalInformation(credentials, false);

        String bankIdCompleteUrl = collectBankId(orderBankIdResponse, bankIdUrl);

        // Authenticate against Norwegian

        String completeBankIdResponse = createClientRequest(bankIdCompleteUrl).get(String.class);

        // Initiate the SAML request.

        Document completeDocument = Jsoup.parse(completeBankIdResponse);
        Element formElement = completeDocument.getElementById("responseForm");

        // Use the SAML created secret key to authenticate the user.

        ClientResponse authenticateClientResponse =
                createClientRequest(formElement.attr("action"))
                        .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                        .entity(ElementUtils.parseFormParameters(formElement))
                        .post(ClientResponse.class);

        // Try to access transaction page and verify that we aren't redirected
        ClientResponse loggedInResponse =
                createClientRequest(CARD_TRANSACTION_URL).get(ClientResponse.class);

        if (authenticateClientResponse.getStatus() == HttpStatusCodes.STATUS_CODE_OK
                && loggedInResponse.getStatus() == HttpStatusCodes.STATUS_CODE_OK) {
            return true;
        } else {
            throw new IllegalStateException(
                    String.format(
                            "#login-refactoring - Norwegian - Did not get status code for both authenticateClientResponse "
                                    + "and loggedInResponse: %s, %s",
                            authenticateClientResponse.getStatus(), loggedInResponse.getStatus()));
        }
    }

    private String collectBankId(OrderBankIdResponse orderBankIdResponse, String bankIdUrl)
            throws BankIdException {
        // Validate authentication.

        CollectBankIdRequest collectBankIdRequest = new CollectBankIdRequest();
        collectBankIdRequest.setOrderRef(orderBankIdResponse.getOrderRef());

        CollectBankIdResponse collectResponse = null;

        // Poll BankID status periodically until the process is complete.

        for (int i = 0; i < AUTHENTICATION_BANK_ID_RETRIES; i++) {
            collectResponse =
                    createClientRequest(bankIdUrl + COLLECT)
                            .type(MediaType.APPLICATION_JSON)
                            .post(CollectBankIdResponse.class, collectBankIdRequest);

            ErrorEntity error = collectResponse.getError();
            if (error != null) {
                switch (error.getCode().toUpperCase()) {
                    case "CANCELLED":
                        throw BankIdError.ALREADY_IN_PROGRESS.exception();
                    case "USER_CANCEL":
                        throw BankIdError.CANCELLED.exception();
                    case "ALREADY_IN_PROGRESS":
                        throw BankIdError.ALREADY_IN_PROGRESS.exception();
                    default:
                        throw new IllegalStateException(
                                String.format(
                                        "#login-refactoring - Norwegian - BankId login failed with unknown error: %s",
                                        error.getCode()));
                }
            }

            if ("COMPLETE".equalsIgnoreCase(collectResponse.getProgressStatus())) {
                break;
            }

            // Only necessary when running locally.
            // log.info(String.format("Awaiting BankID authentication - %s",
            // collectResponse.getProgressStatus()));

            Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
        }

        // Authenticate against Norwegian

        if (!"COMPLETE".equalsIgnoreCase(collectResponse.getProgressStatus())) {
            throw BankIdError.TIMEOUT.exception();
        }

        return collectResponse.getCompleteUrl();
    }

    /**
     * Collect the credit card account
     *
     * @throws AccountNotFoundException if the account could not be found
     */
    private Account getAccount() throws AccountNotFoundException {

        AccountEntity account = new AccountEntity();

        // Parse account number and balance
        String creditcardPage = createClientRequest(CREDIT_CARD_URL).get(String.class);
        account.setAccountNumber(CreditCardParsingUtils.parseAccountNumber(creditcardPage));
        account.setBalance(CreditCardParsingUtils.parseBalance(creditcardPage));

        return account.toTinkAccount();
    }

    private Optional<Account> getSavingsAccount() {
        ClientResponse clientResponse =
                createClientRequest(SAVINGS_ACCOUNTS_URL).get(ClientResponse.class);

        if (clientResponse.getStatus() != HttpStatusCodes.STATUS_CODE_OK) {
            return Optional.empty();
        }

        String savingsAccountPage = clientResponse.getEntity(String.class);

        Optional<String> accountNumber =
                SavingsAccountParsingUtils.parseSavingsAccountNumber(savingsAccountPage);
        if (!accountNumber.isPresent()) {
            return Optional.empty();
        }

        Account account = new Account();
        account.setBalance(
                SavingsAccountParsingUtils.parseSavingsAccountBalance(savingsAccountPage));
        account.setAccountNumber(accountNumber.get());
        // Only one savings account per user is allowed
        account.setBankId("NORWEGIAN_SAVINGS_ACCOUNT");
        account.setName("Norwegian Sparkonto");
        account.setType(AccountTypes.SAVINGS);

        return Optional.of(account);
    }

    private void updateTransactions(Account account) throws Exception {
        CreditCardInfoResponse transactionMainPageContent =
                createClientRequest(CARD_TRANSACTION_URL).get(CreditCardInfoResponse.class);

        String accountNumber = transactionMainPageContent.getAccountNo();

        if (accountNumber == null) {
            log.warn("#norwegian: Could not parse account number when updating transactions.");
            return;
        }

        pageTransactions(account, accountNumber);
    }

    private void pageTransactions(Account account, String accountNumber)
            throws ParseException, UnsupportedEncodingException {

        final String encodedAccountNumber = URLEncoder.encode(accountNumber, "UTF-8");

        // We will paginate from the current date towards the past.
        Date toDate = DateTime.now().toDate();
        Date fromDate = DateUtils.addMonths(toDate, -PAGINATION_MONTH_STEP);

        List<Transaction> transactions = Lists.newArrayList();

        // Fetch uninvoiced transactions and add to list of transactions
        createClientRequest(getFormattedRecentTransactionsUrl(encodedAccountNumber))
                .get(TransactionListResponse.class).stream()
                .map(TransactionEntity::toTransaction)
                .forEach(transactions::add);

        // Page through rest of transactions
        do {
            createClientRequest(getFormattedPaginationUrl(encodedAccountNumber, fromDate, toDate))
                    .get(TransactionListResponse.class).stream()
                    .map(TransactionEntity::toTransaction)
                    .forEach(transactions::add);

            toDate = fromDate;
            fromDate = DateUtils.addMonths(fromDate, -PAGINATION_MONTH_STEP);

            // A hard limit on pagination to stop us from fetching transactions before the birth of
            // christ in
            // the case where 'certainDate' is not set.
            if (fromDate.before(PAGINATION_LIMIT)) {
                break;
            }

        } while (!isContentWithRefresh(account, transactions));

        financialDataCacher.updateTransactions(account, transactions);
    }

    /**
     * Returns request string for fetching uninvoiced transactions. This is done by omitting dates
     * and setting the EOC query param to true.
     */
    private String getFormattedRecentTransactionsUrl(final String accountNo) {
        return TRANSACTIONS_PAGINATION_URL
                .queryParam(QueryKeys.ACCOUNT_NUMBER, accountNo)
                .queryParam(QueryKeys.DATE_TO, "")
                .queryParam(QueryKeys.DATE_FROM, "")
                .queryParam(QueryKeys.CORE_DOWN, QueryValues.CORE_DOWN)
                .queryParam(QueryKeys.GET_LAST_DAYS, QueryValues.GET_LAST_DAYS)
                .queryParam(QueryKeys.FROM_LAST_EOC, QueryValues.FROM_LAST_EOC_TRUE)
                .toString();
    }

    /**
     * Returns request string for fetching transactions through pagination, date params are set and
     * EOC param is set to false (EOC being uninvoiced transactions).
     */
    private String getFormattedPaginationUrl(
            final String accountNo, final Date from, final Date to) {
        return TRANSACTIONS_PAGINATION_URL
                .queryParam(QueryKeys.ACCOUNT_NUMBER, accountNo)
                .queryParam(QueryKeys.DATE_FROM, toFormattedDate(from))
                .queryParam(QueryKeys.DATE_TO, toFormattedDate(to))
                .queryParam(QueryKeys.CORE_DOWN, QueryValues.CORE_DOWN)
                .queryParam(QueryKeys.GET_LAST_DAYS, QueryValues.GET_LAST_DAYS)
                .queryParam(QueryKeys.FROM_LAST_EOC, QueryValues.FROM_LAST_EOC_FALSE)
                .toString();
    }

    private static String toFormattedDate(final Date date) {
        return ThreadSafeDateFormat.FORMATTER_DAILY.format(date);
    }

    private WebResource.Builder createClientRequest(String url) {
        return client.resource(url)
                .header("User-Agent", DEFAULT_USER_AGENT)
                .accept(MediaType.APPLICATION_JSON);
    }

    @Override
    public void logout() throws Exception {
        // NOP
    }
}
