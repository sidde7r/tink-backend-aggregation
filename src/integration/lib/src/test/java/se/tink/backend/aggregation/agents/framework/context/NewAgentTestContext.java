package se.tink.backend.aggregation.agents.framework.context;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.apache.curator.framework.CuratorFramework;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountHolder;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.contexts.ProviderSessionCacheContext;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.framework.dao.AccountDataDao;
import se.tink.backend.aggregation.agents.framework.dao.CredentialDataDao;
import se.tink.backend.aggregation.agents.framework.testserverclient.AgentTestServerClient;
import se.tink.backend.aggregation.agents.framework.utils.CliPrintUtils;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Loan;
import se.tink.backend.aggregation.agents.models.LoanDetails;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.api.AggregatorInfo;
import se.tink.backend.aggregation.fakelogmasker.FakeLogMasker;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.nxgen.framework.validation.AisValidator;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account_data_cache.AccountData;
import se.tink.libraries.account_data_cache.AccountDataCache;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.user.rpc.User;

public final class NewAgentTestContext extends AgentContext {
    private static final Logger log = LoggerFactory.getLogger(NewAgentTestContext.class);
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static final String TEST_CLUSTERID = "oxford-preprod";
    public static final String TEST_APPID = "5f98e87106384b2981c0354a33b51590";

    private final AccountDataCache accountDataCache;
    private final List<Transfer> transfers = new ArrayList<>();
    private IdentityData identityData = null;

    private final User user;
    private Credentials credential;
    private final AgentTestServerClient agentTestServerClient;
    private final SupplementalRequester supplementalRequester;
    private final ProviderSessionCacheContext providerSessionCacheContext;
    private final Provider provider;

    // configuration
    private final int transactionsToPrint;

    public NewAgentTestContext(
            User user,
            Credentials credential,
            SupplementalRequester supplementalRequester,
            ProviderSessionCacheContext providerSessionCacheContext,
            int transactionsToPrint,
            String appId,
            String clusterId,
            Provider provider) {
        this.accountDataCache = new AccountDataCache();
        this.user = user;
        this.credential = credential;
        this.supplementalRequester = supplementalRequester;
        this.providerSessionCacheContext = providerSessionCacheContext;
        this.transactionsToPrint = transactionsToPrint;
        this.provider = provider;
        this.setClusterId(MoreObjects.firstNonNull(clusterId, TEST_CLUSTERID));
        this.setAppId(MoreObjects.firstNonNull(appId, TEST_APPID));
        agentTestServerClient = AgentTestServerClient.getInstance();
        LogMasker logMasker = new FakeLogMasker();
        setLogMasker(logMasker);

        setTestContext(true);
        setAggregatorInfo(AggregatorInfo.getAggregatorForTesting());
    }

    public AgentTestServerClient getAgentTestServerClient() {
        return agentTestServerClient;
    }

    public void setCredential(Credentials credential) {
        this.credential = credential;
    }

    @Override
    public void clear() {
        super.clear();
        accountDataCache.clear();
        transfers.clear();
    }

    public List<Account> getUpdatedAccounts() {
        return Lists.newArrayList(accountDataCache.getProcessedAccounts());
    }

    public List<Transaction> getTransactions() {
        return accountDataCache.getTransactionsToBeProcessed();
    }

    public Optional<IdentityData> getIdentityData() {
        return Optional.ofNullable(identityData);
    }

    public List<Transaction> getTransactionsToProcessByBankAccountId(String bankAccountId) {
        return accountDataCache
                .getProcessedAccountDataByBankAccountId(bankAccountId)
                .map(AccountData::getTransactions)
                .orElse(Lists.newArrayList());
    }

    public List<Transfer> getTransfers() {
        return transfers;
    }

    public List<TransferDestinationPattern> getTransferDestinationPatterns() {
        return accountDataCache.getTransferDestinationPatternsToBeProcessed().values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public void processTransactions() {
        // Should never be called, and we shouldn't have to implement this method to begin with
        throw new AssertionError();
    }

    @Override
    public Optional<String> waitForSupplementalInformation(
            String mfaId, long waitFor, TimeUnit unit) {
        return supplementalRequester.waitForSupplementalInformation(mfaId, waitFor, unit);
    }

    @Override
    public String requestSupplementalInformation(
            Credentials credentials, long waitFor, TimeUnit timeUnit, boolean wait) {
        return supplementalRequester.requestSupplementalInformation(
                credentials, waitFor, timeUnit, wait);
    }

    @Override
    public void openBankId(String autoStartToken, boolean wait) {
        supplementalRequester.openBankId(autoStartToken, wait);
    }

    @Override
    public void cacheAccount(Account account, AccountFeatures accountFeatures) {
        accountDataCache.cacheAccount(account);
        accountDataCache.cacheAccountFeatures(account.getBankId(), accountFeatures);

        // Automatically process it when cached. This is because we don't have a WorkerCommand doing
        // this for us.
        sendAccountToUpdateService(account.getBankId());
    }

    public Account sendAccountToUpdateService(String bankAccountId) {
        Optional<AccountData> optionalAccountData =
                accountDataCache.getFilteredAccountDataByBankAccountId(bankAccountId);
        if (!optionalAccountData.isPresent()) {
            log.warn(
                    "Trying to send a filtered or non-existent Account to update service! Agents should not do this on their own.");
            return null;
        }

        AccountData accountData = optionalAccountData.get();
        if (accountData.isProcessed()) {
            // Already updated/processed, do not do it twice.
            return accountData.getAccount();
        }

        Account account = accountData.getAccount();
        accountDataCache.setProcessedTinkAccountId(bankAccountId, account.getId());
        return account;
    }

    @Override
    public AccountHolder sendAccountHolderToUpdateService(Account processedAccount) {
        return processedAccount.getAccountHolder();
    }

    @Override
    public void updateTransferDestinationPatterns(
            Map<Account, List<TransferDestinationPattern>> transferDestinationPatterns) {

        transferDestinationPatterns.forEach(
                (account, patterns) -> {
                    accountDataCache.cacheAccount(account);
                    accountDataCache.cacheTransferDestinationPatterns(
                            account.getBankId(), patterns);
                });
    }

    @Override
    public void updateStatus(CredentialsStatus status) {
        log.info("Updating status: " + status.name());

        credential.setStatus(status);
    }

    @Override
    public void updateStatus(
            CredentialsStatus status, String statusPayload, boolean statusFromProvider) {
        log.info(
                "Updating status: "
                        + status.name()
                        + " ("
                        + statusPayload
                        + ") - statusFromProvider: "
                        + statusFromProvider);

        credential.setStatus(status);
        credential.setStatusPayload(statusPayload);
    }

    @Override
    @Deprecated // Use cacheTransactions instead
    public Account updateTransactions(Account account, List<Transaction> transactions) {
        cacheAccount(account);
        cacheTransactions(account.getBankId(), transactions);
        return account;
    }

    @Override
    public void cacheTransactions(@Nonnull String accountUniqueId, List<Transaction> transactions) {
        Preconditions.checkNotNull(
                accountUniqueId); // Necessary until we make @Nonnull throw the exception

        accountDataCache.cacheTransactions(accountUniqueId, transactions);
    }

    @Override
    public Catalog getCatalog() {
        return Catalog.getCatalog(this.user.getLocale());
    }

    @Override
    public void updateCredentialsExcludingSensitiveInformation(
            Credentials credentials, boolean doUpdateStatus) {
        // noop
    }

    public CuratorFramework getCoordinationClient() {
        return null;
    }

    @Override
    public MetricRegistry getMetricRegistry() {
        return new MetricRegistry();
    }

    @Override
    public void updateEinvoices(List<Transfer> transfers) {
        this.transfers.addAll(transfers);
    }

    @Override
    public void cacheIdentityData(IdentityData identityData) {
        this.identityData = identityData;
    }

    @Override
    public void sendIdentityToIdentityAggregatorService() {}

    public void validateFetchedData(AisValidator validator) {
        validator.validate(
                accountDataCache.getProcessedAccounts(),
                accountDataCache.getTransactionsToBeProcessed(),
                identityData);
    }

    private void printLoanDetails(List<Loan> loans) {
        List<Map<String, String>> table =
                loans.stream()
                        .map(
                                loan -> {
                                    Map<String, String> row = new LinkedHashMap<>();

                                    row.put(
                                            "type",
                                            Optional.ofNullable(loan.getType())
                                                    .map(Loan.Type::name)
                                                    .orElse(null));
                                    row.put("number", loan.getLoanNumber());
                                    row.put("name", loan.getName());
                                    row.put("balance", String.valueOf(loan.getBalance()));
                                    row.put(
                                            "interest",
                                            CliPrintUtils.formatPercent(loan.getInterest()));

                                    Optional<LoanDetails> details =
                                            Optional.ofNullable(loan.getLoanDetails());
                                    row.put(
                                            "co-applicants",
                                            String.valueOf(
                                                    details.map(LoanDetails::getCoApplicant)
                                                            .orElse(null)));
                                    row.put(
                                            "applicants",
                                            String.valueOf(
                                                    details.map(LoanDetails::getApplicants)
                                                            .orElse(null)));
                                    row.put(
                                            "security",
                                            details.map(LoanDetails::getLoanSecurity).orElse(null));
                                    return row;
                                })
                        .collect(Collectors.toList());
        CliPrintUtils.printTable(4, "loans", table);
    }

    private void printInstrumentDetails(List<Instrument> instruments) {
        List<Map<String, String>> table =
                instruments.stream()
                        .map(
                                instrument -> {
                                    Map<String, String> row = new LinkedHashMap<>();

                                    row.put("uniqueId", instrument.getUniqueIdentifier());
                                    row.put(
                                            "type (raw type)",
                                            String.format(
                                                    "%s (%s)",
                                                    instrument.getType().name(),
                                                    instrument.getRawType()));
                                    row.put("isin", instrument.getIsin());
                                    row.put("ticker", instrument.getTicker());
                                    row.put("name", instrument.getName());
                                    row.put("market", instrument.getMarketPlace());
                                    row.put("price", String.valueOf(instrument.getPrice()));
                                    row.put("value", String.valueOf(instrument.getMarketValue()));
                                    row.put("profit", String.valueOf(instrument.getProfit()));
                                    row.put("quantity", String.valueOf(instrument.getQuantity()));
                                    row.put("currency", String.valueOf(instrument.getCurrency()));
                                    row.put(
                                            "aap",
                                            String.valueOf(
                                                    instrument.getAverageAcquisitionPrice()));

                                    return row;
                                })
                        .collect(Collectors.toList());
        CliPrintUtils.printTable(8, "instruments", table);
    }

    private void printPortfolioDetails(List<Portfolio> portfolios) {
        portfolios.forEach(
                portfolio -> {
                    Map<String, String> row = new LinkedHashMap<>();

                    row.put("uniqueId", portfolio.getUniqueIdentifier());
                    row.put(
                            "type (raw type)",
                            String.format(
                                    "%s (%s)", portfolio.getType().name(), portfolio.getRawType()));
                    row.put("cash value", String.valueOf(portfolio.getCashValue()));
                    row.put("tot. value", String.valueOf(portfolio.getTotalValue()));
                    row.put("tot. profit", String.valueOf(portfolio.getTotalProfit()));
                    CliPrintUtils.printTable(4, "portfolio", Lists.newArrayList(row));

                    if (portfolio.getInstruments() != null) {
                        printInstrumentDetails(portfolio.getInstruments());
                    }
                });
    }

    private void printAccountIdentifiers(Account account) {
        List<Map<String, String>> identifierList = new LinkedList<>();
        account.getIdentifiers()
                .forEach(
                        identifier -> {
                            Map<String, String> row = new LinkedHashMap<>();
                            row.put("name", identifier.getName().orElse(null));
                            row.put("type", identifier.getType().toString());
                            row.put("identifier", identifier.getIdentifier());
                            identifierList.add(row);
                        });
        CliPrintUtils.printTable(4, "identifiers", identifierList);
    }

    private void printAccountInformation(Account account, AccountFeatures accountFeatures) {
        Map<String, String> row = new LinkedHashMap<>();

        row.put("accountId", account.getBankId());
        row.put("type", account.getType().name());
        row.put("number", account.getAccountNumber());
        row.put("name", account.getName());
        row.put("balance", String.valueOf(account.getBalance()));
        if (account.getType() == AccountTypes.CREDIT_CARD) {
            row.put("availableCredit", String.valueOf(account.getAvailableCredit()));
        } else if (account.getType() == AccountTypes.CHECKING
                || account.getType() == AccountTypes.SAVINGS) {
            row.put(
                    "availableBalance",
                    String.valueOf(
                            Optional.ofNullable(account.getAvailableBalance())
                                    .map(ExactCurrencyAmount::getExactValue)
                                    .orElse(null)));
            row.put(
                    "creditLimit",
                    String.valueOf(
                            Optional.ofNullable(account.getCreditLimit())
                                    .map(ExactCurrencyAmount::getExactValue)
                                    .orElse(null)));
        }
        CliPrintUtils.printTable(0, "account", Lists.newArrayList(row));

        printAccountIdentifiers(account);

        switch (account.getType()) {
            case LOAN:
            case MORTGAGE:
                Assert.assertNotNull(accountFeatures.getLoans());
                // Assert.assertFalse(accountFeatures.getLoans().isEmpty());
                printLoanDetails(accountFeatures.getLoans());
                break;

            case INVESTMENT:
                Assert.assertNotNull(accountFeatures.getPortfolios());
                for (Portfolio portfolio : accountFeatures.getPortfolios()) {
                    Assert.assertNotNull(portfolio.getInstruments());
                    // Assert.assertFalse(portfolio.getInstruments().isEmpty());
                }
                printPortfolioDetails(accountFeatures.getPortfolios());
                break;

            default:
                Assert.assertNotNull(accountFeatures.getLoans());
                Assert.assertNotNull(accountFeatures.getPortfolios());
                Assert.assertTrue(accountFeatures.getLoans().isEmpty());
                Assert.assertTrue(accountFeatures.getPortfolios().isEmpty());
                break;
        }
    }

    private void printTransactions(List<Transaction> transactions) {
        List<Map<String, String>> table =
                transactions.stream()
                        .sorted(
                                Comparator.comparing(
                                        Transaction::getDate,
                                        Comparator.nullsLast(Comparator.reverseOrder())))
                        .map(
                                transaction -> {
                                    Map<String, String> row = new LinkedHashMap<>();
                                    if (!Objects.isNull(transaction.getDate())) {
                                        row.put("date", dateFormat.format(transaction.getDate()));
                                    }

                                    row.put("description", transaction.getDescription());
                                    row.put("amount", String.valueOf(transaction.getAmount()));
                                    return row;
                                })
                        .collect(Collectors.toList());
        CliPrintUtils.printTable(4, "transactions", table, transactionsToPrint);
    }

    private void printTransferDestinations(
            List<TransferDestinationPattern> transferDestinationPatterns) {
        List<Map<String, String>> table =
                transferDestinationPatterns.stream()
                        .map(
                                transferDestination -> {
                                    Map<String, String> row = new LinkedHashMap<>();
                                    row.put(
                                            "type",
                                            Optional.ofNullable(transferDestination.getType())
                                                    .map(AccountIdentifier.Type::name)
                                                    .orElse(null));
                                    row.put("bank", transferDestination.getBank());
                                    row.put("name", transferDestination.getName());
                                    row.put("pattern", transferDestination.getPattern());
                                    return row;
                                })
                        .collect(Collectors.toList());
        CliPrintUtils.printTable(4, "transfer destinations", table);
    }

    private void printTransfers() {
        List<Map<String, String>> table =
                transfers.stream()
                        .map(
                                transfer -> {
                                    Map<String, String> row = new LinkedHashMap<>();

                                    row.put("id", transfer.getId().toString());
                                    row.put(
                                            "source",
                                            Optional.ofNullable(transfer.getSource())
                                                    .map(AccountIdentifier::toString)
                                                    .orElse(""));
                                    row.put(
                                            "destination",
                                            Optional.ofNullable(transfer.getDestination())
                                                    .map(AccountIdentifier::toString)
                                                    .orElse(""));
                                    row.put(
                                            "amount",
                                            Optional.ofNullable(transfer.getAmount())
                                                    .map(
                                                            x ->
                                                                    String.format(
                                                                            "%s %s",
                                                                            x.getValue(),
                                                                            x.getCurrency()))
                                                    .orElse(""));

                                    return row;
                                })
                        .collect(Collectors.toList());

        CliPrintUtils.printTable(0, "transfers", table);
    }

    public void printIdentityData() {
        if (identityData != null) {
            List<Map<String, String>> table =
                    identityData.toMap().entrySet().stream()
                            .map(
                                    entry -> {
                                        Map<String, String> row = new LinkedHashMap<>();
                                        row.put("key", entry.getKey());
                                        row.put("value", entry.getValue());
                                        return row;
                                    })
                            .collect(Collectors.toList());

            CliPrintUtils.printTable(0, "identityData", table);
        }
    }

    private void printCredentialsInfo() {
        Map<String, String> row = new LinkedHashMap<>();
        String sessionExpiry =
                Optional.ofNullable(credential.getSessionExpiryDate())
                        .map(dateFormat::format)
                        .orElse("");
        row.put("sessionExpiry", sessionExpiry);
        CliPrintUtils.printTable(0, "credentials", Lists.newArrayList(row));
    }

    public void printCollectedData() {
        accountDataCache
                .getProcessedAccountData()
                .forEach(
                        accountData -> {
                            printAccountInformation(
                                    accountData.getAccount(), accountData.getAccountFeatures());
                            printTransactions(accountData.getTransactions());
                            printTransferDestinations(accountData.getTransferDestinationPatterns());
                            System.out.println();
                        });

        printIdentityData();
        printTransfers();
        printCredentialsInfo();
    }

    public CredentialDataDao dumpCollectedData() {
        List<AccountDataDao> accountDataList = new ArrayList<>();
        accountDataCache
                .getProcessedAccountData()
                .forEach(
                        accountData ->
                                accountDataList.add(
                                        new AccountDataDao(
                                                accountData.getAccount(),
                                                accountData.getTransactions(),
                                                accountData.getTransferDestinationPatterns())));
        return new CredentialDataDao(accountDataList, transfers, identityData);
    }

    @Override
    public String getProviderSessionCache() {
        return providerSessionCacheContext.getProviderSessionCache();
    }

    @Override
    public void setProviderSessionCache(String value, int expiredTimeInSeconds) {
        providerSessionCacheContext.setProviderSessionCache(value, expiredTimeInSeconds);
    }

    public void validateCredentials() {
        Assertions.assertThat(credential).isNotNull();
        Assertions.assertThat(provider).isNotNull();
        if (provider.getAccessType() == Provider.AccessType.OPEN_BANKING) {
            Assertions.assertThat(credential.getSessionExpiryDate())
                    .withFailMessage(
                            "Credentials session expiry date must not be null for provider with access type %s",
                            provider.getAccessType())
                    .isNotNull();
        }
    }
}
