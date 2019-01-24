package se.tink.backend.aggregation.agents.banks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.HttpStatusCodes;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import com.google.common.util.concurrent.Uninterruptibles;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.PersistentLogin;
import se.tink.backend.aggregation.agents.RefreshableItemExecutor;
import se.tink.backend.aggregation.agents.banks.skandiabanken.model.AccountEntity;
import se.tink.backend.aggregation.agents.banks.skandiabanken.model.AccountListResponse;
import se.tink.backend.aggregation.agents.banks.skandiabanken.model.AuthenticateBankIdResponse;
import se.tink.backend.aggregation.agents.banks.skandiabanken.model.CollectBankIdResponse;
import se.tink.backend.aggregation.agents.banks.skandiabanken.model.CollectMessage;
import se.tink.backend.aggregation.agents.banks.skandiabanken.model.ErrorResponse;
import se.tink.backend.aggregation.agents.banks.skandiabanken.model.HoldingEntity;
import se.tink.backend.aggregation.agents.banks.skandiabanken.model.InvestmentEntity;
import se.tink.backend.aggregation.agents.banks.skandiabanken.model.InvestmentResponse;
import se.tink.backend.aggregation.agents.banks.skandiabanken.model.InvestmentsResponse;
import se.tink.backend.aggregation.agents.banks.skandiabanken.model.LoginMethod;
import se.tink.backend.aggregation.agents.banks.skandiabanken.model.LoginMethodsResponse;
import se.tink.backend.aggregation.agents.banks.skandiabanken.model.LoginResponse;
import se.tink.backend.aggregation.agents.banks.skandiabanken.model.PersistentSession;
import se.tink.backend.aggregation.agents.banks.skandiabanken.model.PortfolioEntity;
import se.tink.backend.aggregation.agents.banks.skandiabanken.model.TransactionEntity;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.utils.jersey.LoggingFilter;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.RefreshableItem;
import se.tink.backend.system.rpc.AccountFeatures;
import se.tink.backend.system.rpc.Instrument;
import se.tink.backend.system.rpc.Portfolio;
import se.tink.backend.system.rpc.Transaction;
import se.tink.backend.system.rpc.TransactionTypes;
import se.tink.libraries.i18n.LocalizableEnum;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.net.TinkApacheHttpClient4;
import se.tink.libraries.net.TinkApacheHttpClient4Handler;
import se.tink.libraries.strings.StringUtils;

public class SkandiabankenAgent extends AbstractAgent implements PersistentLogin, RefreshableItemExecutor {
    private static final int MAX_PAGES_LIMIT = 150;
    private static final String BASE_URL_SECURE = "https://login.skandia.se";
    private static final String AUTHENTICATE_WITH_BANKID_AUTOSTART_URL =
            BASE_URL_SECURE + "/mobiltbankid/autostartauthenticate/";
    private static final int BANKID_LOGIN_METHOD_ID = 9;
    private static final String BASE_URL = "https://service2.smartrefill.se/BankServicesSkandia";
    private static final String COLLECT_BANKID_URL = BASE_URL_SECURE + "/mobiltbankid/collecting/";
    private static final String COUNTRY_CODE = "SE";
    private static final String CUSTOMER_OWNER = "SKANDIABANKEN";
    private static final String SERVICE_NAME = "bank";
    private static final int FUND = 1;
    private static final int STOCK = 2;

    // states:
    //  3 - continue polling
    //  4 - cancelled (both user cancelled and already-in-progress)
    //  5 - done
    private static final int BANKID_CONTINUE = 3;
    private static final int BANKID_CANCELLED = 4;
    private static final int BANKID_DONE = 5;
    private static final int NUM_QR_REFRESH_RETRY_ATTEMPTS = 3;

    /**
     * Extract request verification token from a HTML document body.
     * 
     * @param html   the HTML body to extract the request verification token from.
     * @param formId the HTML ID of the form that holds the request verification token.
     * @return a string consisting of the request verification token.
     */
    private static MultivaluedMap<String, String> extractRequestVerificationToken(String html, String formId) {
        MultivaluedMap<String, String> form = new MultivaluedMapImpl();
        String token = Jsoup.parse(html).getElementById(formId)
                .getElementsByAttributeValue("name", "__RequestVerificationToken").attr("value");

        Preconditions.checkNotNull(token);

        form.add("__RequestVerificationToken", token);
        form.add("X-Requested-With", "XMLHttpRequest");

        return form;
    }

    private static String getBaseUrlWithCustomerOwner() {
        return BASE_URL + "/rest/" + SERVICE_NAME + "/" + COUNTRY_CODE + "/" + CUSTOMER_OWNER;
    }

    private final TinkApacheHttpClient4 client;
    private final Credentials credentials;
    private Integer customerId = null;
    private final RateLimiter pagingRateLimiter = RateLimiter.create(2);
    private static final String ACCOUNT_LOG_TEMPLATE = "[accountId:%s] %s";
    private static final ObjectMapper mapper = new ObjectMapper();

    // cache
    private Map<AccountEntity, Account> accounts;

    public SkandiabankenAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context);

        credentials = request.getCredentials();
        client = createCircularRedirectClient();
    }

    private Optional<String> initiateBankID(String loginPageBody) throws BankIdException {
        String authenticateUrl;
        MultivaluedMap<String, String> postData;

        postData = extractRequestVerificationToken(loginPageBody, "autostartbutton-form");
        authenticateUrl = AUTHENTICATE_WITH_BANKID_AUTOSTART_URL;

        AuthenticateBankIdResponse authenticateResponse = createLoginClientRequest(authenticateUrl, true)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(AuthenticateBankIdResponse.class, postData);

        switch (authenticateResponse.getState()) {
        case 2:
            // Authentication with autostarttoken
            String autostartUrl = authenticateResponse.getAutostartUrl();
            return Optional.of(autostartUrl.substring(autostartUrl.indexOf("=") + 1));
        case 3:
            // Normal authentication with BankID
            return Optional.empty();
        case 4:
            // message: "En inloggning för den här personen är redan påbörjad."
            throw BankIdError.ALREADY_IN_PROGRESS.exception();
        default:
            throw new IllegalStateException(String.format("Authentication method not allowed: %s",
                    authenticateResponse.getMessage()));
        }
    }

    private Optional<CollectBankIdResponse> poll(MultivaluedMap<String, String> postData) throws BankIdException {

        for (int i = 0; i < 30; i++) {
            CollectBankIdResponse collectResponse = createLoginClientRequest(COLLECT_BANKID_URL, false).type(
                    MediaType.APPLICATION_FORM_URLENCODED).post(CollectBankIdResponse.class, postData);

            if (collectResponse.getState() == BANKID_DONE) {
                return Optional.of(collectResponse);
            }

            if (collectResponse.getState() == BANKID_CANCELLED) {
                String header = collectResponse.getMessage().getHeader();
                switch (header.toLowerCase()) {
                case "du har valt att avbryta inloggningen":
                    throw BankIdError.CANCELLED.exception();
                case "inloggningen har avbrutits":
                    throw BankIdError.ALREADY_IN_PROGRESS.exception();
                case "bankid inte installerat":
                    return Optional.empty(); // Signal retry for QR-code
                default:
                    throw new IllegalStateException(
                                    String.format(
                                            "SkandiaBanken: Unknown bankId response, header: %s, message: %s",
                                            header,
                                            collectResponse.getMessage().getText()
                                    )
                    );
                }
            }

            Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
        }

        // If we are polling QR code it should have refreshed at this point.
        // If we are using autostarttoken redirect the BankID app will have timed out on its own.
        throw BankIdError.TIMEOUT.exception();
    }

    private LoginResponse authenticateWithBankId(LoginMethod loginMethod)
            throws AuthenticationException, AuthorizationException {
        // Get the login method for BankID.

        final String loginPageBody = createLoginClientRequest(loginMethod.getLoginUrl(), false).get(String.class);

        // Wait for a successful BankID authentication.
        String interAppURL = null;
        MultivaluedMap<String, String> postData = extractRequestVerificationToken(loginPageBody, "collect-form");

        Optional<CollectBankIdResponse> pollResult;
        int attempts = 0;
        do {

            openBankID(initiateBankID(loginPageBody));
            pollResult = poll(postData);
            attempts++;

        } while (attempts < NUM_QR_REFRESH_RETRY_ATTEMPTS && !pollResult.isPresent());

        CollectBankIdResponse collectResponse = pollResult.orElseThrow(BankIdError.TIMEOUT::exception);
        interAppURL = collectResponse.getRedirectUrl();
        if (interAppURL == null) {
            throw BankIdError.TIMEOUT.exception();
        }

        if (interAppURL.startsWith("/login/message")) {
            fetchAndLogMessage(interAppURL);
            throw new IllegalStateException(String.format("#login-refactoring - message %s, state %s",
                    collectResponse.getMessage(), collectResponse.getState()));
        } else if (interAppURL.contains("otpchooser")) {
            throw AuthorizationError.ACCOUNT_BLOCKED.exception(UserMessage.CONFIRM_BANKID.getKey());
        }

        // Extract the authentication URL from inter-app URL.

        String authorizeURL;
        try {
            URI interappUri = new URI(interAppURL);
            List<NameValuePair> queryParts = URLEncodedUtils.parse(interappUri, "UTF-8");
            Optional<NameValuePair> authorizeURLKVPair = queryParts.stream().filter(
                    input -> Objects.equal(input.getName(), "url")).findFirst();

            if (!authorizeURLKVPair.isPresent()) {
                String errorInfo = null;
                if (collectResponse.getMessage() != null) {
                    CollectMessage message = collectResponse.getMessage();
                    if (StringUtils.trimToNull(message.getText()) != null) {
                        // Message is more verbose. Go with that if possible.
                        errorInfo = message.getText();
                    } else if (StringUtils.trimToNull(message.getHeader()) != null) {
                        errorInfo = message.getHeader();
                    }
                }
                throw new IllegalStateException(
                        String.format("#login-refactoring - Login failed with info: %s",
                        errorInfo != null ? errorInfo : "errorInfo is null"));
            }

            authorizeURL = authorizeURLKVPair.get().getValue();

        } catch (URISyntaxException e) {
            throw new IllegalStateException(String.format("Unable to parse the inter-app URI: %s", interAppURL), e);
        }

        // Authenticate the user.

        ClientResponse clientLoginResponse = createClientRequest(authorizeURL).post(ClientResponse.class);

        String redirectUrl = clientLoginResponse.getHeaders().getFirst("Location");
        if (Strings.isNullOrEmpty(redirectUrl)) {
            throw new IllegalStateException("Missing redirect to logged in page");
        }

        MultivaluedMap<String, String> params = parseAccessTokensFromUrl(redirectUrl);

        clientLoginResponse.close();

        LoginResponse loginResponse;
        try {
            loginResponse = createClientRequest(getBaseUrlWithCustomerOwner() + "/login").post(LoginResponse.class,
                    params);
        } catch (UniformInterfaceException e) {

            ErrorResponse errorResponse = e.getResponse().getEntity(ErrorResponse.class);
            int statusCode = e.getResponse().getStatus();
            // Error Codes that we have seen so far
            // 451 => Thrown when a new account is created and user has not activated the account at SkandiaBanken
            // 403 => Problem to login. More information in error message. One error message is
            // "Den här versionen av mobila plånboken stöds inte längre. Var god uppdatera.". In that case we need to
            // update the x-smartrefill-marketing-version version.

            String requestParameters = MoreObjects.toStringHelper("requestData")
                    .add("state", params.get("state"))
                    .add("access_code", params.get("access_code"))
                    .toString();

            log.warn(String.format("Caught exception with HttpStatusCode %s while verifying access tokens: %s",
                    statusCode, requestParameters), e);

            Preconditions.checkNotNull(errorResponse);
            throw new IllegalStateException(
                    String.format("#login-refactoring - Skandiabanken - Login failed with message %s",
                            errorResponse.getMessage()));
        }

        return loginResponse;
    }

    private void fetchAndLogMessage(String interAppURL) {
        try {
            String url = BASE_URL_SECURE + interAppURL;
            String messageResponse = createClientRequest(url).get(String.class);
            String content = Jsoup.parse(messageResponse).getElementById("bodyfield").text();
            log.error("Could not login Skandia user.", new IllegalStateException(content));
        } catch (Exception e) {
            log.error("Could not login Skandia user.", e);
        }
    }

    private Builder createClientRequest(String url) {
        Builder requestBuilder = client.resource(url)
                .header("x-smartrefill-version", "654")
                .header("x-smartrefill-inflow", "iphone")
                .header("x-smartrefill-device", StringUtils.hashAsUUID(request.getUser().getId()).toLowerCase())
                .header("x-smartrefill-api-version", "2")
                .header("x-smartrefill-company", "SKANDIABANKEN")
                .header("x-smartrefill-country", COUNTRY_CODE)
                .header("x-smartrefill-marketing-version", "3.1.3")
                .header("x-smartrefill-application", "se.skandia.skandia")
                .header("x-smartrefill-language", "sv")
                .header("User-Agent", DEFAULT_USER_AGENT);

        if (customerId != null) {
            requestBuilder = requestBuilder.header("x-smartrefill-customer", Integer.toString(customerId));
        }

        return requestBuilder;
    }

    private Builder createLoginClientRequest(String url, boolean xhr) {
        Builder requestBuilder = client.resource(url)
                .header("User-Agent", DEFAULT_USER_AGENT);

        if (xhr) {
            requestBuilder.header("X-Requested-With", "XMLHttpRequest");
        }

        return requestBuilder;
    }

    private LoginMethod extractLoginMethod(List<LoginMethod> loginMethods, final int pinLoginMethodId) {
        return Iterables.find(loginMethods, input -> input.getTypeOfLogin() == pinLoginMethodId);
    }

    private void fetchMoreAccountTransactions(String skandiabankenAccountId, Account account, String fwdKey,
            List<Transaction> accumulatedTransactions) {

        // First page has already been fetched, start fetching from page 2
        int page = 2;

        AccountEntity accountEntity;
        boolean isContentWithRefresh;
        do {

            if (page > MAX_PAGES_LIMIT)
                // Throwing an exception here to not deliver transactions back for processing. If this is an infinite
                // loop
                // we risk storing a bunch of duplicate transactions.
                throw new IllegalStateException("Too many pages. Did we reach an infinite loop?");

            final String accountUrl = getBaseUrlWithCustomerOwner() + "/customer/" + customerId + "/account/"
                    + skandiabankenAccountId + "/next?fwdKey=" + fwdKey + "&page=" + page;
            pagingRateLimiter.acquire();
            accountEntity = createClientRequest(accountUrl).get(AccountEntity.class);

            accumulatedTransactions.addAll(getListOfTransactions(accountEntity));

            context.updateStatus(CredentialsStatus.UPDATING, account, accumulatedTransactions);

            // Prepare for a possible next loop.

            fwdKey = accountEntity.getFwdKey();
            page++;

            isContentWithRefresh = isContentWithRefresh(account, accumulatedTransactions);

        } while (!Strings.isNullOrEmpty(fwdKey) && !isContentWithRefresh);

        if (Strings.isNullOrEmpty(fwdKey))
            log.debug(String.format(ACCOUNT_LOG_TEMPLATE, skandiabankenAccountId, "Breaking due to missing fwdKey."));
        if (isContentWithRefresh)
            log.debug(String.format(ACCOUNT_LOG_TEMPLATE, skandiabankenAccountId,
                            "Breaking because we are contentWithRefresh."));
    }

    private List<LoginMethod> getLoginMethods() {
        return createClientRequest(getBaseUrlWithCustomerOwner() + "/").get(LoginMethodsResponse.class)
                .getLoginMethods();
    }

    /**
     * Generate a map from query string.
     *
     * @param query a query string like "hej=1&yo=2". Can be extracted using {@link URL#getQuery()}.
     * @return a map containing the key/values.
     */
    private static ListMultimap<String, String> getQueryMap(String query) {
        String[] params = query.split("&");
        ListMultimap<String, String> map = ArrayListMultimap.create();
        for (String param : params) {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            map.put(name, value);
        }
        return map;
    }

    private MultivaluedMap<String, String> parseAccessTokensFromUrl(String loginUrl) {
        MultivaluedMap<String, String> oauth2Credentials = new MultivaluedMapImpl();

        URL url;
        try {
            url = new URL(loginUrl);
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        ListMultimap<String, String> queryParams = getQueryMap(url.getQuery());

        String oauth2State = queryParams.get("state").get(0);
        String oauth2AccessCode = queryParams.get("code").get(0);

        if (!Strings.isNullOrEmpty(oauth2State) && !Strings.isNullOrEmpty(oauth2AccessCode)) {
            oauth2Credentials.add("state", oauth2State);
            oauth2Credentials.add("access_code", oauth2AccessCode);
        } else {
            // Have never seen this, but one day it might happen...
            throw new IllegalStateException(
                    String.format("Skandiabanken - Oauth2State %s, Oauth2Accesscode %s",
                    oauth2State == null, oauth2AccessCode == null));
        }

        return oauth2Credentials;
    }

    /**
     * Parse and convert the transactions.
     * <p>
     * Package-local visibility for testability.
     */
    static Transaction parseAccountTransaction(TransactionEntity transactionEntity) {

        Transaction transaction = new Transaction();

        // There are cases when no `date` is set. In that case we fall back to settled.
        final String dateToUse = Optional.ofNullable(transactionEntity.getDate())
                .orElse(transactionEntity.getSettled());
        transaction.setDate(parseDate(dateToUse, true));

        String description = transactionEntity.getMerchant();

        if (description.startsWith("Kortköp ")) {
            description = description.substring(8);
            transaction.setType(TransactionTypes.CREDIT_CARD);
            transaction.setPending(true);

            if (description.length() > 22) {
                description = description.substring(0, 22);
            }
        }
        
        if (transactionEntity.getSettled() == null) {
            transaction.setPending(true);
        }
            
        transaction.setDescription(description);
        transaction.setAmount(parseAmount(transactionEntity.getAmount()));

        return transaction;
    }

    private Map<AccountEntity, Account> getAccounts() {
        if (accounts != null) {
            return accounts;
        }

        String accountsUrl = getBaseUrlWithCustomerOwner() + "/customer/" + customerId;

        ClientResponse clientResponse = createClientRequest(accountsUrl).get(ClientResponse.class);
        if (clientResponse.getStatus() != HttpStatusCodes.STATUS_CODE_OK) {
            return Collections.emptyMap();
        }

        AccountListResponse response = clientResponse.getEntity(AccountListResponse.class);
        if (response == null || response.getBankAccounts() == null) {
            return Collections.emptyMap();
        }
        accounts = response.getBankAccounts().stream()
                .filter(a -> !a.isInvestment())
                .collect(Collectors.toMap(Function.identity(), AccountEntity::toAccount));
        return accounts;
    }

    private void refreshInvestmentAccounts() {
        String investmentsUrl = getBaseUrlWithCustomerOwner() + "/customer/" + customerId + "/investments";
        InvestmentsResponse investmentsResponse = createClientRequest(investmentsUrl).get(InvestmentsResponse.class);

        Map<String, Account> accountById = investmentsResponse.stream().collect(Collectors.toMap(
                InvestmentEntity::getId,
                InvestmentEntity::toAccount));

        Map<String, Portfolio> portfolioById = investmentsResponse.stream().collect(Collectors.toMap(
                InvestmentEntity::getId,
                InvestmentEntity::toPortfolio));

        accountById.forEach((id, account) -> {
            InvestmentResponse investmentResponse = createClientRequest(
                    investmentsUrl + "/" + id).get(InvestmentResponse.class);

            List<Instrument> instruments = Lists.newArrayList();

            investmentResponse.getPortfolios().stream()
                    .map(PortfolioEntity::getHoldings)
                    .flatMap(Collection::stream)
                    .filter(h -> Objects.equal(FUND, h.getType()))
                    .map(HoldingEntity::fundToInstrument)
                    .forEach(h -> h.ifPresent(instruments::add));

            investmentResponse.getPortfolios().stream()
                    .map(PortfolioEntity::getHoldings)
                    .flatMap(Collection::stream)
                    .filter(h -> Objects.equal(STOCK, h.getType()))
                    .map(HoldingEntity::stockToInstrument)
                    .forEach(h -> h.ifPresent(instruments::add));

            Portfolio portfolio = portfolioById.get(id);
            if (portfolio == null) {
                log.error("portfolio was null");
                return;
            }

            portfolio.setCashValue(investmentResponse.getDisposableAmount());
            portfolio.setInstruments(instruments);
            context.cacheAccount(account, AccountFeatures.createForPortfolios(portfolio));
        });
    }

    private void updateAccountsPerType(RefreshableItem type) {
        getAccounts().entrySet().stream()
                .filter(set -> type.isAccountType(set.getValue().getType()))
                .forEach(set -> context.cacheAccount(set.getValue()));
    }

    private void updateTransactionsPerAccountType(RefreshableItem type) {
        getAccounts().entrySet().stream()
                .filter(set -> type.isAccountType(set.getValue().getType()))
                .forEach(set -> updateAccountAndTransactions(set.getKey().getId(), set.getValue()));
    }

    @Override
    public void refresh(RefreshableItem item) {
        switch (item) {
        case CHECKING_ACCOUNTS:
        case SAVING_ACCOUNTS:
        case CREDITCARD_ACCOUNTS:
            updateAccountsPerType(item);
            break;

        case CHECKING_TRANSACTIONS:
        case SAVING_TRANSACTIONS:
        case CREDITCARD_TRANSACTIONS:
            updateTransactionsPerAccountType(item);
            break;

        case INVESTMENT_ACCOUNTS:
            try {
                refreshInvestmentAccounts();
            } catch (Exception e) {
                // Just catch and exit gently
                log.warn("Caught exception while logging investment data", e);
            }
            break;
        }
    }

    private void updateAccountAndTransactions(String skandiabankenAccountId, Account account) {
        String accountUrl = getBaseUrlWithCustomerOwner() + "/customer/" + customerId + "/account/"
                + skandiabankenAccountId;

        pagingRateLimiter.acquire();
        AccountEntity accountEntity = createClientRequest(accountUrl).get(AccountEntity.class);

        // List of transactions

        final List<Transaction> accumulatedTransactions = getListOfTransactions(accountEntity);

        final boolean isContentWithRefresh = isContentWithRefresh(account, accumulatedTransactions);

        if (accumulatedTransactions.size() > 0 && !Strings.isNullOrEmpty(accountEntity.getFwdKey())
                && !isContentWithRefresh) {

            fetchMoreAccountTransactions(skandiabankenAccountId, account, accountEntity.getFwdKey(),
                    accumulatedTransactions);
        } else {
            if (accumulatedTransactions.isEmpty()) {
                log.debug(String.format(ACCOUNT_LOG_TEMPLATE, skandiabankenAccountId,
                                "Breaking because we found no transactions."));
            }
            if (Strings.isNullOrEmpty(accountEntity.getFwdKey())) {
                log.debug(
                        String.format(ACCOUNT_LOG_TEMPLATE, skandiabankenAccountId, "Breaking due to missing fwdKey."));
            }
            if (isContentWithRefresh) {
                log.debug(String.format(ACCOUNT_LOG_TEMPLATE, skandiabankenAccountId,
                                "Breaking because we are contentWithRefresh."));
            }
        }

        context.updateTransactions(account, accumulatedTransactions);
    }

    private List<Transaction> getListOfTransactions(AccountEntity accountEntity) {
        return accountEntity.getTransactions().stream()
                .map(SkandiabankenAgent::parseAccountTransaction)
                .collect(Collectors.toList());
    }

    private TinkApacheHttpClient4 createCircularRedirectClient() {
        BasicCookieStore cookieStore = new BasicCookieStore();

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(30000)
                .setConnectTimeout(10000)
                .setSocketTimeout(10000)
                .setCircularRedirectsAllowed(true)
                .setMaxRedirects(20)
                // .setCookieSpec(CookieSpecs.BEST_MATCH) // Not sure I need to set this?
                .build();

        CloseableHttpClient apacheClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieStore(cookieStore)
                .build();

        TinkApacheHttpClient4Handler tinkJerseyApacheHttpsClientHandler = new TinkApacheHttpClient4Handler(
                apacheClient, cookieStore, false);
        TinkApacheHttpClient4 tinkJerseyClient = new TinkApacheHttpClient4(tinkJerseyApacheHttpsClientHandler);

        try {
            tinkJerseyClient
                    .addFilter(new LoggingFilter(new PrintStream(context.getLogOutputStream(), true, "UTF-8")));
        } catch (UnsupportedEncodingException e) {
            log.warn("Could not add buffered logging filter.");
        }

        tinkJerseyClient.setChunkedEncodingSize(null);

        return tinkJerseyClient;
    }

    @Override
    public boolean login() throws AuthenticationException, AuthorizationException {

        List<LoginMethod> loginMethods = getLoginMethods();

        Preconditions.checkArgument(credentials.getType() == CredentialsTypes.MOBILE_BANKID);
        LoginResponse loginResponse = authenticateWithBankId(extractLoginMethod(loginMethods, BANKID_LOGIN_METHOD_ID));

        Preconditions.checkNotNull(loginResponse);

        // Make sure the user always logs in to the same login to not mistakenly suddenly get the wrong transactions.
        final String customerIdString = String.valueOf(loginResponse.getId());
        final String previousCustomerId = credentials.getPayload();

        if (previousCustomerId != null) {
            if (!Objects.equal(customerIdString, previousCustomerId)) {
                throw LoginError.NOT_CUSTOMER.exception(UserMessage.WRONG_BANKID.getKey());
            }
        }

        credentials.setPayload(customerIdString);
        context.updateCredentialsExcludingSensitiveInformation(credentials, false);

        customerId = loginResponse.getId();

        return true;
    }

    @Override
    public void logout() throws Exception {
        // not implemented
    }

    @Override
    public boolean isLoggedIn() throws Exception {
        return keepAlive();
    }

    @Override
    public boolean keepAlive() throws Exception {
        return customerId != null && !getAccounts().isEmpty();
    }

    @Override
    public void persistLoginSession() {

        PersistentSession persistentSession = new PersistentSession();
        persistentSession.setCustomerId(customerId);
        persistentSession.setCookiesFromClient(client);

        credentials.setPersistentSession(persistentSession);
    }

    @Override
    public void loadLoginSession() {

        PersistentSession persistentSession = credentials.getPersistentSession(PersistentSession.class);

        if (persistentSession != null) {
            this.customerId = persistentSession.getCustomerId();
            addSessionCookiesToClient(client, persistentSession);
        }

    }

    @Override
    public void clearLoginSession() {

        // Clean the session in memory
        this.customerId = null;

        // Clean the persisted session
        credentials.removePersistentSession();
    }

    private void openBankID(Optional<String> autostartToken) {
        credentials.setSupplementalInformation(autostartToken.orElse(null));
        credentials.setStatus(CredentialsStatus.AWAITING_MOBILE_BANKID_AUTHENTICATION);
        context.requestSupplementalInformation(credentials, false);
    }

    private enum UserMessage implements LocalizableEnum {
        CONFIRM_BANKID(new LocalizableKey(
                "You need to confirm your BankID to Skandiabanken. You do it by logging in with the Skandiabanken app once to confirm that the BankID is yours. You may then continue using BankID here.")),
        WRONG_BANKID(new LocalizableKey("Wrong BankID signature. Did you log in with the wrong personnummer?")),
        UNDERAGE(new LocalizableKey(
                "Could not login to Skandiabanken. Unfortunately we don't support Skandiabanken for customers under the age of 18 years."));

        private LocalizableKey userMessage;

        UserMessage(LocalizableKey userMessage) {
            this.userMessage = userMessage;
        }

        @Override
        public LocalizableKey getKey() {
            return userMessage;
        }
    }
}
