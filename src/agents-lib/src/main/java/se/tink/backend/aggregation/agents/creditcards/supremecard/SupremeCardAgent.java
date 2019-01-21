package se.tink.backend.aggregation.agents.creditcards.supremecard;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Uninterruptibles;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.representation.Form;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.DeprecatedRefreshExecutor;
import se.tink.backend.aggregation.agents.creditcards.supremecard.model.AccountResponse;
import se.tink.backend.aggregation.agents.creditcards.supremecard.model.TransactionEntity;
import se.tink.backend.aggregation.agents.creditcards.supremecard.model.TransactionsResponse;
import se.tink.backend.aggregation.agents.creditcards.supremecard.model.UserResponse;
import se.tink.backend.aggregation.agents.creditcards.supremecard.model.v2.CollectEntity;
import se.tink.backend.aggregation.agents.creditcards.supremecard.model.v2.CollectRequest;
import se.tink.backend.aggregation.agents.creditcards.supremecard.model.v2.OrderEntity;
import se.tink.backend.aggregation.agents.creditcards.supremecard.model.v2.OrderRequest;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.utils.jsoup.ElementUtils;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.system.rpc.Transaction;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.date.DateUtils;

public class SupremeCardAgent extends AbstractAgent implements DeprecatedRefreshExecutor {
    private static final AggregationLogger log = new AggregationLogger(SupremeCardAgent.class);
    private static final Locale SWEDISH_LOCALE = new Locale("sv", "SE");
    private boolean hasRefreshed = false;

    protected Builder createClientRequest(String uri) {
        return client.resource(uri).header("User-Agent", DEFAULT_USER_AGENT).accept("*/*").acceptLanguage("sv-se");
    }

    private Client client;

    public SupremeCardAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context);

        client = clientFactory.createCookieClient(context.getLogOutputStream());
    }

    /**
     * After redirects, the supreme card do two redirects that ends up in a html page with info about token and some
     * service identifier which we need in order to check status of login.
     */
    @Override
    public boolean login() throws AuthenticationException, AuthorizationException {
        OrderEntity orderEntity = startNewSession();

        launchBankIdApp(orderEntity.getAutoStartToken());
        return authenticateBankId(orderEntity.getCollectUrl(), orderEntity.getOrderRef());
    }

    private OrderEntity startNewSession() {
        client.setFollowRedirects(true);
        ClientResponse startSessionResponse = createClientRequest(
                "https://www.supremecard.se/elogin-handler").get(ClientResponse.class);

        if (startSessionResponse.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
            String bankIdTypeChooserPageHtml = startSessionResponse.getEntity(String.class);
            String bankIdLauncherPageUrl = getAutostartUrlFromBankIdTypeChooserPage(bankIdTypeChooserPageHtml);
            String bankIdLauncherPageHtml = createClientRequest(bankIdLauncherPageUrl).get(String.class);

            Pattern p = Pattern.compile("(?s)signicat.serviceUrl = '(.+?)';");
            Matcher m = p.matcher(bankIdLauncherPageHtml);
            if (m.find()) {
                String serviceUrl = m.group(1);

                Preconditions.checkNotNull(serviceUrl, "Service url could not be parsed from html body.");
                Preconditions.checkArgument(serviceUrl.startsWith("https://eid.resurs.com/std/method/resurs/"),
                        "Could not parse url from html body. Unexpected url.");
                Preconditions.checkArgument(serviceUrl.endsWith("/"),
                        "Could not parse url from http body. Missing end slash.");

                String orderUrl = serviceUrl + "order";
                ClientResponse response = createClientRequest(orderUrl).type(MediaType.APPLICATION_JSON).post(
                        ClientResponse.class, new OrderRequest());

                OrderEntity orderEntity = response.getEntity(OrderEntity.class);
                Preconditions.checkNotNull(orderEntity);
                Preconditions.checkNotNull(orderEntity.getAutoStartToken());
                Preconditions.checkNotNull(orderEntity.getCollectUrl());
                Preconditions.checkNotNull(orderEntity.getOrderRef());
                return orderEntity;
            }
        }

        throw new IllegalStateException("Could not start session for bank id auth with Supreme Card.");
    }

    /**
     * Extract autostart link for current device from html. There are two links, one which is bound to SSN and one with
     * autostarttoken. No special ID tagging or similar, so just do text matching.
     *
     * <div id="content-wrapper">
     *     <div id="mainContent">
     *         <div class='signicat-portal'><b class='signicat-portal-header'>Välj eID</b><div class='signicat-portal-text'>Välj vilket eID du vill använda för att logga in. Om du har några frågor hänvisar vi till kundservice.</div><ul class='signicat-portal-list'><li class='signicat-portal-list-entry'><a class='signicat-portal-list-entry-link' href="https://eid.resurs.com/std/method/resurs/9b0eff59a73a409abc92ee4051cb4de5ddd73fa0c40511e5bf140050569120ff/?newmethod=sbid-mobil">BankId på annan mobil enhet</a></li><li class='signicat-portal-list-entry'><a class='signicat-portal-list-entry-link' href="https://eid.resurs.com/std/method/resurs/9b0eff59a73a409abc92ee4051cb4de5ddd73fa0c40511e5bf140050569120ff/?newmethod=sbid">BankId på denna enhet</a></li></ul></div>
     *     </div>
     * </div>
     *
     * @return BankId autostart url
     */
    private String getAutostartUrlFromBankIdTypeChooserPage(String landingPageHtml) {
        Document completeDocument = Jsoup.parse(landingPageHtml);

        Elements bankIdLinks = completeDocument.getElementsByClass("signicat-portal-list-entry-link");

        for (Element bankIdLink : bankIdLinks) {
            if (bankIdLink.text().toLowerCase(SWEDISH_LOCALE).contains("på denna enhet")) {
                return bankIdLink.attr("href");
            }
        }

        throw new IllegalStateException("Could not find BankId url from landing page");
    }

    private void launchBankIdApp(String autoStartToken) {
        Credentials credentials = request.getCredentials();
        credentials.setSupplementalInformation(Preconditions.checkNotNull(autoStartToken));
        credentials.setStatus(CredentialsStatus.AWAITING_MOBILE_BANKID_AUTHENTICATION);

        supplementalRequester.requestSupplementalInformation(credentials, false);
    }

    /**
     * Polls bankId status for successful authentication, activates the session and 
     * validates the user identifier.   
     * 
     */
    private boolean authenticateBankId(String collectUrl, String orderRef) throws BankIdException {
        CollectRequest request = new CollectRequest();
        request.setOrderRef(orderRef);

        for (int i = 0; i < 45; i++) {
            CollectEntity collectEntity = createClientRequest(collectUrl).type(
                    MediaType.APPLICATION_JSON).post(CollectEntity.class, request);

            Preconditions.checkNotNull(collectEntity);

            log.info("Awaiting Mobil BankId sign: " + collectEntity.getProgressStatus());

            log.debug(
                    String.format(
                            "Temporary logging for debugging - progressStatus: %s, errorCode: %s, errorMessage: %s",
                            collectEntity.getProgressStatus(),
                            collectEntity.getError() != null ? collectEntity.getError().getCode() : null,
                            collectEntity.getError() != null ? collectEntity.getError().getMessage() : null));
            if (collectEntity.isAuthenticated()) {
                return activateSession(request, collectEntity);
            }

            if (!collectEntity.shouldContinuePolling()) {
                log.info(String.format(
                        "#login-refactoring - [BankId login failed with errorCode]: %s [processStatus] %s",
                        collectEntity.getError().getCode(), collectEntity.getProgressStatus()));
                throw BankIdError.TIMEOUT.exception();
            }

            Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
        }

        throw BankIdError.TIMEOUT.exception();
    }

    private boolean activateSession(CollectRequest collectRequest, CollectEntity collectEntity) {
        String completeUrl = collectEntity.getCompleteUrl();

        // Request the complete url.

        String completeUrlResponse = createClientRequest(completeUrl).type(
                MediaType.APPLICATION_JSON).post(String.class, collectRequest);

        Document completeDocument = Jsoup.parse(completeUrlResponse);
        Element formElement = completeDocument.getElementById("responseForm");

        // Request the SAML token.

        ClientResponse authenticateClientResponse = createClientRequest(formElement.attr("action"))
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .entity(ElementUtils.parseFormParameters(formElement))
                .post(ClientResponse.class);

        // Redirect to url.

        createClientRequest(authenticateClientResponse.getHeaders().getFirst("Location")).get(String.class);

        // Make sure the user always logs in to the same login to not mistakenly suddenly get the wrong transactions.

        if (!isCorrectUserIdentifier()) {
            return false;
        }

        return true;
    }

    /**
     * Gets the account numbers from this person number login and created a user identifier to validate this
     * is a login from the with the same person number as last time. 
     *  
     */
    private boolean isCorrectUserIdentifier() {
        UserResponse userResponse = createClientRequest(
                "https://www.supremecard.se/wp-content/plugins/rb.bank.connector/ajax/userInfo.php").post(
                UserResponse.class);

        if (userResponse.getData() == null) {
            statusUpdater.updateStatus(CredentialsStatus.AUTHENTICATION_ERROR, "No user information.");
            return false;
        }
        
        Set<String> accountNumbers = Sets.newHashSet();

        // User account number (not same as card number). Is probably one per user, but handling multiple as well.

        for (int j = 0; j < userResponse.getData().getNumberAccounts(); j++) {
            accountNumbers.add(userResponse.getData().getAccounts().get(j).getAccountNumber());
        }

        String userIdentifier = accountNumbers.stream().sorted().collect(Collectors.joining());

        final String previousUserIdentifier = this.request.getCredentials().getPayload();

        if (previousUserIdentifier != null) {
            if (!Objects.equal(userIdentifier, previousUserIdentifier)) {
                statusUpdater.updateStatus(
                        CredentialsStatus.AUTHENTICATION_ERROR,
                        context.getCatalog().getString(
                                "Wrong BankID signature. Did you log in with the wrong personnummer?"));
                return false;
            }
        } else {
            this.request.getCredentials().setPayload(userIdentifier);
            context.updateCredentialsExcludingSensitiveInformation(this.request.getCredentials(), false);
        }
        return true;
    }

    private Account refreshAccount() {

        AccountResponse accountResponse = createClientRequest(
                "https://www.supremecard.se/wp-content/plugins/rb.bank.connector/ajax/accountInfo.php").post(
                AccountResponse.class);

        if (accountResponse.getData() == null) {
            return null;
        }

        Account account = new Account();

        account.setBalance(-(parseAmount(accountResponse.getData().getPositiveBalance()) + parseAmount(accountResponse
                .getData().getReservedAmount())));
        account.setAvailableCredit(parseAmount(accountResponse.getData().getApprovedCredit()));
        account.setName(StringUtils.formatHuman(accountResponse.getData().getName()));
        account.setAccountNumber(accountResponse.getData().getAccountNumber());
        account.setBankId(accountResponse.getData().getAccountNumber());
        account.setType(AccountTypes.CREDIT_CARD);

        Preconditions.checkState(
                Preconditions.checkNotNull(account.getBankId()).matches("[0-9]{16}"),
                "Unexpected account.bankid '%s'. Reformatted?", account.getBankId());

        return account;
    }

    @Override
    public void refresh() throws Exception {
        // The refresh command will call refresh multiple times.
        // This check ensures the refresh only runs once.
        if (hasRefreshed) {
            return;
        }
        hasRefreshed = true;

        Account account = refreshAccount();

        if (account == null) {
            return;
        }

        List<Transaction> transactions = refreshTransactions(account);

        financialDataCacher.updateTransactions(account, transactions);
    }

    private List<Transaction> refreshTransactions(Account account) {
        List<Transaction> transactions = Lists.newArrayList();

        Calendar calendar = DateUtils.getCalendar();
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        for (int i = 0; i < 24; i++) {
            Form form = new Form();

            form.add("month", calendar.get(Calendar.MONTH) + 1);
            form.add("year", calendar.get(Calendar.YEAR));

            calendar.add(Calendar.MONTH, -1);

            TransactionsResponse transactionsResponse = createClientRequest(
                    "https://www.supremecard.se/wp-content/plugins/rb.bank.connector/ajax/transactions.php")
                    .post(TransactionsResponse.class, form);

            if (transactionsResponse.getData() == null) {
                continue;
            }

            for (TransactionEntity transactionEntity : transactionsResponse.getData()) {
                transactions.add(transactionEntity.toTransaction());
            }

            statusUpdater.updateStatus(CredentialsStatus.UPDATING, account, transactions);

            if (isContentWithRefresh(account, transactions)) {
                break;
            }
        }

        return transactions;
    }

    @Override
    public void logout() throws Exception {
    }
}
