package se.tink.backend.aggregation.agents.framework.context;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
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
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountHolder;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.Provider;
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
import se.tink.backend.aggregation.agents.models.fraud.FraudDetailsContent;
import se.tink.backend.aggregation.api.AggregatorInfo;
import se.tink.backend.aggregation.fakelogmasker.FakeLogMasker;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.framework.validation.AisValidator;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account_data_cache.AccountData;
import se.tink.libraries.account_data_cache.AccountDataCache;
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
    private final Map<String, Account> accountsByBankId = new HashMap<>();
    private final Map<String, AccountFeatures> accountFeaturesByBankId = new HashMap<>();
    private final List<Transfer> transfers = new ArrayList<>();
    private IdentityData identityData = null;

    private final User user;
    private final Credentials credential;
    private final AgentTestServerClient agentTestServerClient;
    private final SupplementalRequester supplementalRequester;
    private final Provider provider;

    // configuration
    private final int transactionsToPrint;

    public NewAgentTestContext(
            User user,
            Credentials credential,
            SupplementalRequester supplementalRequester,
            int transactionsToPrint,
            String appId,
            String clusterId,
            Provider provider) {
        this.accountDataCache = new AccountDataCache();
        this.user = user;
        this.credential = credential;
        this.supplementalRequester = supplementalRequester;
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

    @Override
    public void clear() {
        super.clear();

        accountDataCache.clear();
        accountsByBankId.clear();
        accountFeaturesByBankId.clear();
        transfers.clear();
    }

    public List<Account> getUpdatedAccounts() {
        return Lists.newArrayList(accountsByBankId.values());
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
            String key, long waitFor, TimeUnit unit) {
        return supplementalRequester.waitForSupplementalInformation(key, waitFor, unit);
    }

    @Override
    public String requestSupplementalInformation(Credentials credentials, boolean wait) {
        return supplementalRequester.requestSupplementalInformation(credentials, wait);
    }

    @Override
    public void openBankId(String autoStartToken, boolean wait) {
        supplementalRequester.openBankId(autoStartToken, wait);
    }

    @Override
    public void cacheAccount(Account account, AccountFeatures accountFeatures) {
        accountDataCache.cacheAccount(account);
        accountDataCache.cacheAccountFeatures(account.getBankId(), accountFeatures);

        accountsByBankId.put(account.getBankId(), account);

        AccountFeatures accountFeaturesToCache = accountFeatures;

        if (accountFeaturesByBankId.containsKey(account.getBankId())) {
            // FIXME: this check mirrors the check done in AgentWorkerContext.cacheAccount that is
            // needed
            // FIXME: because some agents still need to call updateTransactions, which still needs
            // to call the
            // FIXME: default cacheAccount method, which then clobbers the existing AccountFeatures
            // cache.
            // FIXME: Once the problem has been fixed in production code, we can remove this check
            // as well.
            AccountFeatures existingAccountFeatures =
                    accountFeaturesByBankId.get(account.getBankId());
            if (accountFeatures.isEmpty() && !existingAccountFeatures.isEmpty()) {
                accountFeaturesToCache = existingAccountFeatures;
            }
        }

        accountFeaturesByBankId.put(account.getBankId(), accountFeaturesToCache);
    }

    public Account sendAccountToUpdateService(String bankAccountId) {
        return accountsByBankId.get(bankAccountId);
    }

    @Override
    public AccountHolder sendAccountHolderToUpdateService(Account processedAccount) {
        return accountsByBankId.values().stream()
                .filter(a -> Objects.equals(processedAccount.getId(), a.getId()))
                .findFirst()
                .map(Account::getAccountHolder)
                .orElse(null);
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
                        + String.valueOf(statusFromProvider));

        credential.setStatus(status);
        credential.setStatusPayload(statusPayload);
    }

    @Override
    @Deprecated // Use cacheTransactions instead
    public Account updateTransactions(Account account, List<Transaction> transactions) {
        cacheAccount(account);
        final Account updatedAccount = sendAccountToUpdateService(account.getBankId());

        for (Transaction updatedTransaction : transactions) {
            updatedTransaction.setAccountId(updatedAccount.getId());
            updatedTransaction.setCredentialsId(updatedAccount.getCredentialsId());
            updatedTransaction.setUserId(updatedAccount.getUserId());
        }

        cacheTransactions(updatedAccount.getBankId(), transactions);
        return updatedAccount;
    }

    @Override
    public void cacheTransactions(@Nonnull String accountUniqueId, List<Transaction> transactions) {
        Preconditions.checkNotNull(
                accountUniqueId); // Necessary until we make @Nonnull throw the exception
        accountDataCache.cacheTransactions(accountUniqueId, transactions);
    }

    @Override
    public void updateFraudDetailsContent(List<FraudDetailsContent> contents) {
        throw new NotImplementedException("Fraud cannot be tested yet.");
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

    @Override
    public void updateCredentialsExcludingSensitiveInformation(
            Credentials credentials, boolean doStatusUpdate, boolean isMigrationUpdate) {
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
                accountsByBankId.values(),
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
                                    row.put("interest", String.valueOf(loan.getInterest()));

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

    private void printAccountInformation(Account account) {
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
                                    .map(a -> a.getExactValue())
                                    .orElse(null)));
            row.put(
                    "creditLimit",
                    String.valueOf(
                            Optional.ofNullable(account.getCreditLimit())
                                    .map(a -> a.getExactValue())
                                    .orElse(null)));
        }
        CliPrintUtils.printTable(0, "account", Lists.newArrayList(row));

        printAccountIdentifiers(account);

        AccountFeatures accountFeatures = accountFeaturesByBankId.get(account.getBankId());

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

    private void printTransactions(String bankId) {
        List<Map<String, String>> table =
                getTransactionsToProcessByBankAccountId(bankId).stream()
                        .sorted(Comparator.comparing(Transaction::getDate))
                        .map(
                                transaction -> {
                                    Map<String, String> row = new LinkedHashMap<>();
                                    row.put("date", dateFormat.format(transaction.getDate()));
                                    row.put("description", transaction.getDescription());
                                    row.put("amount", String.valueOf(transaction.getAmount()));
                                    return row;
                                })
                        .collect(Collectors.toList());
        CliPrintUtils.printTable(4, "transactions", table, transactionsToPrint);
    }

    private void printTransferDestinations(String bankId) {
        Optional<AccountData> optionalAccountData =
                accountDataCache.getProcessedAccountDataByBankAccountId(bankId);
        if (!optionalAccountData.isPresent()) {
            return;
        }
        AccountData accountData = optionalAccountData.get();

        List<Map<String, String>> table =
                accountData.getTransferDestinationPatterns().stream()
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

    public void printCollectedData() {
        accountsByBankId.forEach(
                (bankId, account) -> {
                    printAccountInformation(account);
                    printTransactions(bankId);
                    printTransferDestinations(bankId);
                    System.out.println();
                });

        printIdentityData();
        printTransfers();
    }

    public CredentialDataDao dumpCollectedData() {
        List<AccountDataDao> accountDataList = new ArrayList<>();
        accountDataCache
                .getProcessedAccountData()
                .forEach(
                        accountData -> {
                            accountDataList.add(
                                    new AccountDataDao(
                                            accountData.getAccount(),
                                            accountData.getTransactions(),
                                            accountData.getTransferDestinationPatterns()));
                        });
        return new CredentialDataDao(accountDataList, transfers, identityData);
    }

    @Override
    public String getProviderSessionCache() {
        log.info(
                "Requesting provider session cache info for Financial institution id: {} from client.",
                provider.getFinancialInstitutionId());
        return agentTestServerClient.getProviderSessionCache(provider.getFinancialInstitutionId());
    }

    @Override
    public void setProviderSessionCache(String value, int expiredTimeInSeconds) {
        agentTestServerClient.setProviderSessionCache(
                provider.getFinancialInstitutionId(), value, expiredTimeInSeconds);
    }
}
