package se.tink.backend.aggregation.agents.creditcards.supremecard.v2;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Uninterruptibles;
import com.sun.jersey.api.client.ClientResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.http.client.utils.URIBuilder;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.creditcards.supremecard.v2.SupremeCardApiConstants.TimeoutConfig;
import se.tink.backend.aggregation.agents.creditcards.supremecard.v2.model.AccountInfoEntity;
import se.tink.backend.aggregation.agents.creditcards.supremecard.v2.model.AccountInfoResponse;
import se.tink.backend.aggregation.agents.creditcards.supremecard.v2.model.ErrorEntity;
import se.tink.backend.aggregation.agents.creditcards.supremecard.v2.model.TransactionEntity;
import se.tink.backend.aggregation.agents.creditcards.supremecard.v2.rpc.CollectBankIdResponse;
import se.tink.backend.aggregation.agents.creditcards.supremecard.v2.rpc.OrderBankIdResponse;
import se.tink.backend.aggregation.agents.creditcards.supremecard.v2.rpc.SamlRequest;
import se.tink.backend.aggregation.agents.creditcards.supremecard.v2.rpc.TransactionsRequest;
import se.tink.backend.aggregation.agents.creditcards.supremecard.v2.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.utils.jersey.NoRedirectStrategy;
import se.tink.backend.aggregation.agents.utils.jersey.filter.JerseyTimeoutRetryFilter;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.utils.SupplementalInformationUtils;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.i18n.LocalizableEnum;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.net.client.TinkApacheHttpClient4;
import se.tink.libraries.serialization.utils.SerializationUtils;

@AgentCapabilities(generateFromImplementedExecutors = true)
public final class SupremeCardAgent extends AbstractAgent
        implements RefreshCreditCardAccountsExecutor {
    private static final int MAX_ATTEMPTS = 65;
    private final Credentials credentials;
    private final SupremeCardApiAgent apiAgent;

    // cache
    private AccountInfoEntity accountInfoEntity = null;
    private Account account = null;

    public SupremeCardAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context);

        this.apiAgent = createApiAgent();
        this.credentials = request.getCredentials();
    }

    private SupremeCardApiAgent createApiAgent() {
        TinkApacheHttpClient4 client =
                clientFactory.createClientWithRedirectHandler(
                        context.getLogOutputStream(), new NoRedirectStrategy());
        client.addFilter(
                new JerseyTimeoutRetryFilter(
                        TimeoutConfig.NUM_TIMEOUT_RETRIES,
                        TimeoutConfig.TIMEOUT_RETRY_SLEEP_MILLISECONDS));
        return SupremeCardApiAgent.createApiAgent(client);
    }

    @Override
    public boolean login() throws Exception {
        switch (credentials.getType()) {
            case MOBILE_BANKID:
                return loginWithBankId();
            default:
                throw new IllegalStateException("Not implemented");
        }
    }

    private Optional<String> askUserForSsn() {
        final String supplementalResponseKey = "response";
        Field field =
                Field.builder()
                        .description("Personnummer")
                        .name(supplementalResponseKey)
                        .numeric(true)
                        .hint("ÅÅÅÅMMDDXXXX")
                        .maxLength(12)
                        .minLength(12)
                        .pattern("^(19|20)\\d{2}(0\\d|1[0-2])([0-2]\\d|3[0-1])\\d{4}$") // SSN regex
                        .build();

        credentials.setSupplementalInformation(
                SerializationUtils.serializeToString(Lists.newArrayList(field)));
        credentials.setStatus(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION);

        String response = supplementalRequester.requestSupplementalInformation(credentials);

        return SupplementalInformationUtils.getResponseFields(response, supplementalResponseKey);
    }

    private boolean loginWithBankId() throws BankIdException, LoginException {
        // Historically we have been using autostartToken to authenticate users (the credential
        // doesn't contain a ssn)
        // But since their app stopped working and their website doesn't allow autostartToken
        // authentication with
        // mobile devices, we need to ask the user for their ssn and update it on the credential
        if (Strings.isNullOrEmpty(credentials.getField(Field.Key.USERNAME))) {
            Optional<String> ssn = askUserForSsn();

            if (!ssn.isPresent()) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }

            credentials.setField(Field.Key.USERNAME, ssn.get());
            systemUpdater.updateCredentialsExcludingSensitiveInformation(credentials, false);

            // Due to a bug in the app, we aren't able to prompt BankID after a supplemental
            // information request
            // So instead we need to abort the current login operation and ask the user to try again
            throw LoginError.INCORRECT_CREDENTIALS.exception(UserMessage.SSN_UPDATED.getKey());
        }

        // The first thing we have to do is to initiate the bankId.
        // Here we'll get two necessary urls to be used as referers.
        Map<String, String> initiateBankIdMap = initiateBankId();

        // All the steps in the bank id login uses a dynamic referer.
        Optional<String> optionalBankIdRefererUrl =
                SupremeCardParsingUtils.parseBankIdUrl(initiateBankIdMap.get("htmlResponse"));
        if (!optionalBankIdRefererUrl.isPresent()) {
            throw new IllegalStateException("#supreme-card - Could not find bank id referer url.");
        }
        String bankIdRefererUrl = optionalBankIdRefererUrl.get();

        // Before we can do the order of a bankId login we have to initiate the bankId session.
        // From this will get signicat information which will be used as query parameters in the
        // referer for when we collect bankId status.
        String initiateBankIdResponse =
                initiateBankIdLogin(bankIdRefererUrl, initiateBankIdMap.get("referer"));

        Optional<Map<String, String>> optionalSignicatFields =
                SupremeCardParsingUtils.parseSignicatFields(initiateBankIdResponse);

        if (!optionalSignicatFields.isPresent()) {
            throw new IllegalStateException("#supreme-card - Could parse signicat fields.");
        }

        Map<String, String> signicatFieldsMap = optionalSignicatFields.get();
        String bankIdBaseUrl =
                signicatFieldsMap.get(SupremeCardApiConstants.SIGNICAT_SERVICE_URL_KEY);

        // Finally we can order bankId
        OrderBankIdResponse orderBankIdResponse = orderBankId(bankIdBaseUrl, bankIdRefererUrl);

        // Before we collect bankId we need to create the referer.
        URI bankIdCollectReferer = createBankIdCollectRefererQueryURI(signicatFieldsMap);

        // Time to collect bankId;
        openBankID();
        CollectBankIdResponse collectBankIdResponse =
                collectBankId(bankIdCollectReferer, orderBankIdResponse);

        // Complete bankId login
        return completeBankId(bankIdCollectReferer, orderBankIdResponse, collectBankIdResponse);
    }

    private Map<String, String> initiateBankId() {
        // Go to the base url, which will redirect us.
        ClientResponse initiateBankIdResponse = apiAgent.initiateBankId();

        // Follow the redirect.
        ClientResponse followRedirect =
                apiAgent.followInitiateBankIdRedirect(
                        initiateBankIdResponse.getLocation().toString());
        // Now we can closed the initial stream.
        initiateBankIdResponse.close();

        // Follow the redirect. From this response we can parse out the referer for a future
        // request.
        ClientResponse bankIdLoginUrlResponse =
                apiAgent.followInitiateBankIdRedirect(followRedirect.getLocation().toString());

        ClientResponse bankIdLoginUrlResponse2 =
                apiAgent.followSecondBankIdRedirect(
                        bankIdLoginUrlResponse.getLocation().toString());

        Map<String, String> initiateBankIdMap = Maps.newHashMap();
        initiateBankIdMap.put("referer", followRedirect.getLocation().toString());
        initiateBankIdMap.put("htmlResponse", bankIdLoginUrlResponse2.getEntity(String.class));

        followRedirect.close();
        bankIdLoginUrlResponse.close();

        return initiateBankIdMap;
    }

    private String initiateBankIdLogin(String url, String referer) {
        return apiAgent.initiateBankIdLogin(url, referer).getEntity(String.class);
    }

    private URI createBankIdCollectRefererQueryURI(Map<String, String> signicatFieldsMap) {
        try {
            String ticket = signicatFieldsMap.get(SupremeCardApiConstants.SIGNICAT_TICKET_KEY);
            String server = signicatFieldsMap.get(SupremeCardApiConstants.SIGNICAT_SERVER_KEY);
            String serviceUrl =
                    signicatFieldsMap.get(SupremeCardApiConstants.SIGNICAT_SERVICE_URL_KEY);

            if (ticket == null || server == null) {
                throw new IllegalStateException(
                        String.format(
                                "#supreme-card - ticket or server string where null - ticket: %s, server: %s",
                                ticket == null, server == null));
            }

            if (ticket.isEmpty() || server.isEmpty()) {
                throw new IllegalStateException(
                        String.format(
                                "#supreme-card - ticket or server string where empty - ticket: %s, server: %s",
                                ticket.isEmpty(), server.isEmpty()));
            }

            URIBuilder uriBuilder = new URIBuilder(serviceUrl);
            uriBuilder.addParameter(SupremeCardApiConstants.SIGNICAT_TICKET_URL_QUERY_KEY, ticket);
            uriBuilder.addParameter(SupremeCardApiConstants.SIGNICAT_SERVER_URL_QUERY_KEY, server);

            return uriBuilder.build();
        } catch (URISyntaxException e) {
            throw new AssertionError(); // Can't happen;
        }
    }

    private OrderBankIdResponse orderBankId(String bankIdBaseUrl, String bankIdRefererUrl)
            throws BankIdException {
        OrderBankIdResponse orderBankIdResponse =
                apiAgent.orderBankId(
                        bankIdBaseUrl, bankIdRefererUrl, credentials.getField(Field.Key.USERNAME));

        ErrorEntity error = orderBankIdResponse.getError();
        if (error != null) {
            switch (error.getCode().toLowerCase()) {
                case SupremeCardApiConstants.BANKID_STATUS_ALREADY_IN_PROGRESS:
                    throw BankIdError.ALREADY_IN_PROGRESS.exception();
                default:
                    throw new IllegalStateException(
                            String.format(
                                    "#supreme-card - never before seen error code: %s",
                                    error.getCode()));
            }
        }
        return orderBankIdResponse;
    }

    private CollectBankIdResponse collectBankId(
            URI bankIdRefererURI, OrderBankIdResponse orderBankIdResponse) throws BankIdException {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            CollectBankIdResponse collectBankIdResponse =
                    apiAgent.collectBankId(bankIdRefererURI.toString(), orderBankIdResponse);

            switch (collectBankIdResponse.getProgressStatus().toLowerCase()) {
                case SupremeCardApiConstants.BANKID_STATUS_COMPLETE:
                    return collectBankIdResponse;
                case SupremeCardApiConstants.BANKID_STATUS_ALREADY_IN_PROGRESS:
                    throw BankIdError.ALREADY_IN_PROGRESS.exception();
                case SupremeCardApiConstants.BANKID_STATUS_OUTSTANDING_TRANSACTION:
                    // intentional fall through
                case SupremeCardApiConstants.BANKID_STATUS_USER_SIGN:
                    break;
                case SupremeCardApiConstants.BANKID_STATUS_NO_CLIENT:
                    throw BankIdError.NO_CLIENT.exception();
                default:
                    throw new IllegalStateException(
                            String.format(
                                    "#supreme-card - bankid status not implemented: %s",
                                    collectBankIdResponse.getProgressStatus()));
            }

            Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
        }

        throw BankIdError.TIMEOUT.exception();
    }

    private boolean completeBankId(
            URI bankIdRefererURI,
            OrderBankIdResponse orderBankidResponse,
            CollectBankIdResponse collectBankIdResponse) {
        ClientResponse clientResponse =
                apiAgent.completeBankId(
                        bankIdRefererURI.toString(), orderBankidResponse, collectBankIdResponse);

        Optional<Map<String, String>> optionalSamlResponse =
                SupremeCardParsingUtils.parseSAMLResponseAndTargetURL(
                        clientResponse.getEntity(String.class));

        if (!optionalSamlResponse.isPresent()) {
            throw new IllegalStateException(
                    "#supreme-card - could not parse query parameters to complete bankid login");
        }

        Map<String, String> samlResponse = optionalSamlResponse.get();

        ClientResponse finishBankIdLoginResponse =
                apiAgent.finishBankIdLogin(
                        samlResponse.get(SupremeCardApiConstants.TARGET_PARAMETER_KEY),
                        bankIdRefererURI.toString(),
                        SamlRequest.from(samlResponse));

        ClientResponse finishBankIdLoginRedirect =
                apiAgent.followFinishBankIdLoginRedirect(
                        finishBankIdLoginResponse.getLocation().toString(),
                        bankIdRefererURI.toString());
        finishBankIdLoginResponse.close();

        ClientResponse bankIdLoginFinished =
                apiAgent.followFinishBankIdLoginRedirect(
                        finishBankIdLoginRedirect.getLocation().toString(),
                        bankIdRefererURI.toString());

        finishBankIdLoginRedirect.close();
        bankIdLoginFinished.close();

        return true;
    }

    @Override
    public void logout() throws Exception {}

    private void fetchAccountInfo() {
        AccountInfoResponse accountInfoResponse = apiAgent.fetchAccountInfo();

        if (!accountInfoResponse.getSuccess()) {
            throw new IllegalStateException("#supreme-card - failed to fetch account info");
        }

        accountInfoEntity = accountInfoResponse.getData();
        account = accountInfoEntity.toAccount();
    }

    private AccountInfoEntity getAccountInfoEntity() {
        if (accountInfoEntity != null) {
            return accountInfoEntity;
        }

        fetchAccountInfo();
        return accountInfoEntity;
    }

    private Account getAccount() {
        if (account != null) {
            return account;
        }

        fetchAccountInfo();
        return account;
    }

    private enum UserMessage implements LocalizableEnum {
        SSN_UPDATED(
                new LocalizableKey(
                        "The social security number has been updated, please try again"));

        private final LocalizableKey key;

        UserMessage(LocalizableKey key) {
            this.key = key;
        }

        @Override
        public LocalizableKey getKey() {
            return key;
        }
    }
    /////// Refresh Executor Refactor ////////

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return new FetchAccountsResponse(Collections.singletonList(getAccount()));
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return fetchTransactions();
    }

    private FetchTransactionsResponse fetchTransactions() {
        Map<Account, List<Transaction>> transactionsMap = new HashMap<>();
        AccountInfoEntity accountInfoEntity = getAccountInfoEntity();
        Account account = getAccount();

        LocalDate nowInLocalDate =
                LocalDate.now(TimeZone.getTimeZone("Europe/Stockholm").toZoneId());
        int year = nowInLocalDate.getYear();
        int month = nowInLocalDate.getMonthValue();

        List<Transaction> transactions = Lists.newArrayList();
        int failCounter = 0;
        do {
            TransactionsResponse transactionsResponse =
                    apiAgent.fetchTransactions(TransactionsRequest.from(year, month));

            if (month == 1) {
                month = 12;
                year--;
            } else {
                month--;
            }

            // No need to search beyond the creation of the card
            if (YearMonth.of(year, month)
                            .compareTo(
                                    YearMonth.of(
                                            Integer.valueOf(accountInfoEntity.getCreatedYear()),
                                            Integer.valueOf(accountInfoEntity.getCreatedMonth())))
                    < 0) {
                break;
            }

            if (!transactionsResponse.getSuccess() || transactionsResponse.getData() == null) {
                failCounter++;
                continue;
            }

            // Only handle consecutive fails
            if (failCounter != 0) {
                failCounter = 0;
            }

            transactions.addAll(
                    transactionsResponse.getData().stream()
                            .map(TransactionEntity::toTransaction)
                            .collect(Collectors.toList()));
        } while (failCounter < 3 && !isContentWithRefresh(account, transactions));
        transactionsMap.put(account, transactions);
        return new FetchTransactionsResponse(transactionsMap);
    }

    // Should be fixed in, it looks like myPage has changed
    // https://tinkab.atlassian.net/browse/TC-3266
    //    @Override
    //    public FetchIdentityDataResponse fetchIdentityData() {
    //        String myPage = apiAgent.fetchMyPage();
    //        String customerName = SupremeCardParsingUtils.parseName(myPage);
    //
    //        return new FetchIdentityDataResponse(
    //                SeIdentityData.of(customerName, credentials.getField(Key.USERNAME)));
    //    }

    //////////////////////////////////////////
}
