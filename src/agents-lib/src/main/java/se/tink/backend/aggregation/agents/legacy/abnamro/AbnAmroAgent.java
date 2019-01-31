package se.tink.backend.aggregation.agents.abnamro;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Uninterruptibles;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.abnamro.converters.AccountConverter;
import se.tink.backend.aggregation.agents.abnamro.ics.mappers.TransactionMapper;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.rpc.RefreshableItem;
import se.tink.libraries.abnamro.client.EnrollmentClient;
import se.tink.libraries.abnamro.client.IBSubscriptionClient;
import se.tink.libraries.abnamro.client.exceptions.IcsException;
import se.tink.libraries.abnamro.client.model.PfmContractEntity;
import se.tink.libraries.abnamro.client.model.creditcards.CreditCardAccountContainerEntity;
import se.tink.libraries.abnamro.client.model.creditcards.CreditCardAccountEntity;
import se.tink.libraries.abnamro.client.model.creditcards.TransactionContainerEntity;
import se.tink.libraries.abnamro.client.rpc.enrollment.CollectEnrollmentResponse;
import se.tink.libraries.abnamro.client.rpc.enrollment.InitiateEnrollmentResponse;
import se.tink.libraries.abnamro.config.AbnAmroConfiguration;
import se.tink.libraries.abnamro.utils.AbnAmroUtils;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.phonenumbers.InvalidPhoneNumberException;
import se.tink.libraries.phonenumbers.utils.PhoneNumberUtils;
import se.tink.libraries.user.rpc.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/** This is the new AbnAmroAgent that will be used for Grip 3.0. It also includes ICS. */
public class AbnAmroAgent extends AbstractAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshCreditCardAccountsExecutor {
    private static final Minutes AUTHENTICATION_TIMEOUT = Minutes.minutes(5);

    private final Credentials credentials;
    private final Catalog catalog;
    private final User user;
    private final AggregationLogger log = new AggregationLogger(AbnAmroAgent.class);

    private IBSubscriptionClient subscriptionClient;
    private EnrollmentClient enrollmentService;
    private AbnAmroConfiguration abnAmroConfiguration;
    private Map<Long, CreditCardAccountEntity> accountEntities = Maps.newHashMap();
    private List<Account> accounts = null;
    private List<Account> existingAccounts = null;
    private final Integer OLD_ICS_ID_LENGTH = 16;

    public AbnAmroAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context);
        this.user = request.getUser();
        this.credentials = request.getCredentials();
        this.catalog = Catalog.getCatalog(user.getLocale());
        this.existingAccounts = request.getAccounts();
    }
    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        this.abnAmroConfiguration = getValidAbnAmroConfiguration(configuration);

        this.subscriptionClient = new IBSubscriptionClient(abnAmroConfiguration, context.getMetricRegistry());

        this.enrollmentService = new EnrollmentClient(
                clientFactory.createBasicClient(context.getLogOutputStream()),
                abnAmroConfiguration.getEnrollmentConfiguration(),
                context.getMetricRegistry());
    }

    private AbnAmroConfiguration getValidAbnAmroConfiguration(AgentsServiceConfiguration configuration) {
        if (Objects.nonNull(configuration.getAbnAmro())) {
            return configuration.getAbnAmro();
        }

        String clusterIdentifier = Optional.ofNullable(context.getClusterId())
                .orElseThrow(() -> new IllegalStateException("Failed to fetch cluster identifier."));

        switch (clusterIdentifier.toLowerCase()) {
        case "leeds-staging":
            return configuration.getAbnAmroStaging();
        case "leeds-production":
            return configuration.getAbnAmroProduction();
        default:
            throw new IllegalStateException("This agent can only be used by Leeds cluster.");
        }
    }

    @Override
    public boolean login() throws Exception {
        return isAuthenticated() || authenticateWithMobileBanking();
    }

    /**
     * User is authenticated if we have a the customer number stored in the payload.
     */
    private boolean isAuthenticated() {
        return AbnAmroUtils.isValidBcNumberFormat(credentials.getPayload());
    }

    /**
     * Authenticate with the Mobile Banking App. The authentication is performed in four steps
     * 1) Initiate the enrollment for Grip against ABN AMRO.
     * 2) Let the Grip app redirect to Mobile Banking app.
     * 3) User signs/accept the T&C in the Mobile Banking app.
     * 4) Collect/poll the status of the signing.
     */
    private boolean authenticateWithMobileBanking() throws InvalidPhoneNumberException {
        final String phoneNumber = PhoneNumberUtils.normalize(user.getUsername());

        log.info("Authenticating with mobile banking.");
        InitiateEnrollmentResponse response = enrollmentService.initiate(phoneNumber);

        openThirdPartyApp(MobileBankingAuthenticationPayload.create(catalog, response.getToken()));

        log.debug("Polling for mobile banking signing completed.");
        Optional<String> bcNumber = collect(response.getToken());
        log.debug(String.format("Got bcnumber %s.", bcNumber.orElse("failed")));

        // Reset supplemental and status payload
        credentials.setSupplementalInformation(null);
        credentials.setStatusPayload(null);

        if (bcNumber.isPresent()) {
            credentials.setPayload(bcNumber.get());
            credentials.setStatus(CredentialsStatus.UPDATING);
        } else {
            log.error("Failed to receive bc number from ABN.");
            credentials.setStatus(CredentialsStatus.AUTHENTICATION_ERROR);
        }

        systemUpdater.updateCredentialsExcludingSensitiveInformation(credentials, true);

        return bcNumber.isPresent();
    }

    /**
     * Collect/poll the enrollment service until is is completed or timed out.
     */
    private Optional<String> collect(String signingToken) {
        final DateTime start = new DateTime();

        int retry = 0;

        while (start.plus(AUTHENTICATION_TIMEOUT).isAfterNow()) {
            CollectEnrollmentResponse response = enrollmentService.collect(signingToken);

            if (response.isCompleted()) {
                log.info("User enrolled in Mobile Banking.");
                return Optional.ofNullable(response.getBcNumber());
            }

            log.debug(String.format("User enrollment in progress (Retry = '%d').", retry));

            retry++;
            Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);
        }

        log.warn("User enrollment timed out.");
        return Optional.empty();
    }

    private List<Account> getAccounts() {
        if (accounts != null) {
            return accounts;
        }

        final String bcNumber = credentials.getPayload();

        Preconditions.checkState(AbnAmroUtils.isValidBcNumberFormat(bcNumber));
        try {
            List<PfmContractEntity> contracts = subscriptionClient.getContracts(bcNumber);
            accounts = new AccountConverter().convert(contracts);
            return accounts;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private Double getCreditCardBalance(Account account) throws IcsException {
        Long accountNumber = getCreditCardContractNumber(account);
        Optional<CreditCardAccountEntity> accountEntity = getCreditCardAccountEntities(accountNumber);

        return accountEntity.map(acc -> -(acc.getCurrentBalance()+acc.getAuthorizedBalance())).orElse(0.0);
    }

    private List<Transaction> getCreditCardTransactions(Long accountNumber) throws IcsException {
        Optional<CreditCardAccountEntity> accountEntity = getCreditCardAccountEntities(accountNumber);

        return accountEntity.map(acc -> icsTransactionsConverter(acc.getTransactions()))
                .orElseGet(ImmutableList::of);
    }

    private Optional<CreditCardAccountEntity> getCreditCardAccountEntities(Long accountNumber) throws IcsException {
        if (accountEntities.containsKey(accountNumber)) {
            // We've already fetched this account recently.
            return Optional.of(accountEntities.get(accountNumber));
        }

        if (credentials.isDebug()) {
            log.info(String.format("Fetching ICS transactions for contract: %s", accountNumber));
        }

        String bcNumber = credentials.getPayload();

        List<CreditCardAccountEntity> entities = subscriptionClient
                .getCreditCardAccountAndTransactions(bcNumber, accountNumber)
                .stream()
                .map(CreditCardAccountContainerEntity::getCreditCardAccount)
                .collect(Collectors.toList());

        if (entities.size() != 1) {
            log.warn(String.format("Fetching ICS accounts, expected 1 account in payload, got %d", entities.size()));
            return Optional.empty();
        }

        CreditCardAccountEntity accountEntity = entities.get(0);
        accountEntities.put(accountNumber, accountEntity);
        // We're only asking for a single account, so we're only interested in the first account entity.
        return Optional.of(accountEntity);
    }

    private Long getCreditCardContractNumber(Account account) {
        // The contract number is an extension of the account number suffixed by a card specific number. When a user
        // is issued a new card, the contract number changes although the account stays the same. We store the latest
        // available full contract number in the payload to fetch data from ICS but map only on the account number.
        return Long.valueOf(account.getPayload(AbnAmroUtils.ABN_AMRO_ICS_ACCOUNT_CONTRACT_PAYLOAD));
    }

    private List<Transaction> icsTransactionsConverter(List<TransactionContainerEntity> transactionContainer) {
        return transactionContainer.stream()
                .filter(TransactionContainerEntity::isInEUR)
                .map(TransactionMapper::toTransaction)
                .collect(Collectors.toList());
    }

    private void closeExcludeOldDuplicateICSAccounts(List<Account> importedAccounts) {
        existingAccounts.stream()
                .filter(a -> Objects.equals(a.getType(), AccountTypes.CREDIT_CARD))
                .filter(a -> a.getBankId().length() == OLD_ICS_ID_LENGTH)
                .filter(a -> importedAccounts.stream().anyMatch(isOldICSAccount(a)))
                .forEach(a -> {
                    a.setBankId(a.getBankId().concat("-duplicate"));
                    a.setExcluded(true);
                    a.setClosed(true);
                    financialDataCacher.cacheAccount(a);
                    context.sendAccountToUpdateService(a.getBankId());
                });
    }

    private Predicate<Account> isOldICSAccount(Account possiblyOld) {
        return newlyImported -> !Strings.isNullOrEmpty(newlyImported.getPayload()) &&
                newlyImported.getPayload().contains(possiblyOld.getBankId());
    }

    @Override
    public void logout() throws Exception {
        // NOP
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return fetchAccountPerType(RefreshableItem.CHECKING_ACCOUNTS);
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return new FetchTransactionsResponse(Collections.emptyMap());
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        List<Account> resultAccounts = new ArrayList<>();
        List<Account> importedAccounts = getAccounts();
        importedAccounts.stream()
                .filter(a -> Objects.equals(a.getType(), AccountTypes.CREDIT_CARD))
                .forEach(resultAccounts::add);
        closeExcludeOldDuplicateICSAccounts(importedAccounts);
        return new FetchAccountsResponse(resultAccounts);
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {

        try {
            Map<Account, List<Transaction>> transactionsMap = new HashMap<>();
            List<Account> accounts = fetchCreditCardAccounts().getAccounts();
            for (Account account : accounts) {
                account.setBalance(getCreditCardBalance(account));
                context.sendAccountToUpdateService(account.getBankId());
                Long accountNumber = getCreditCardContractNumber(account);
                List<Transaction> transactions = getCreditCardTransactions(accountNumber);
                transactionsMap.put(account, transactions);
            }
            return new FetchTransactionsResponse(transactionsMap);
        } catch (IcsException e) {
            if (abnAmroConfiguration.shouldIgnoreCreditCardErrors()) {
                log.warn("Ignoring error from ICS", e);
                return new FetchTransactionsResponse(Collections.emptyMap());
            } else {
                throw new IllegalStateException(e);
            }
        }
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return fetchAccountPerType(RefreshableItem.SAVING_ACCOUNTS);
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return new FetchTransactionsResponse(Collections.emptyMap());
    }

    private FetchAccountsResponse fetchAccountPerType(RefreshableItem type) {
        List<Account> accounts = new ArrayList<>();
        getAccounts().stream()
                .filter(account -> type.isAccountType(account.getType()))
                .forEach(accounts::add);
        return new FetchAccountsResponse(accounts);
    }
}
