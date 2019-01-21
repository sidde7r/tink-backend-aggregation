package se.tink.backend.aggregation.agents.framework;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.apache.curator.framework.CuratorFramework;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.framework.validation.AisValidator;
import se.tink.backend.aggregation.api.AggregatorInfo;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.backend.aggregation.rpc.User;
import se.tink.backend.core.DocumentContainer;
import se.tink.backend.core.FraudDetailsContent;
import se.tink.backend.core.account.TransferDestinationPattern;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.system.rpc.AccountFeatures;
import se.tink.backend.system.rpc.Instrument;
import se.tink.backend.system.rpc.Loan;
import se.tink.backend.system.rpc.LoanDetails;
import se.tink.backend.system.rpc.Portfolio;
import se.tink.backend.system.rpc.Transaction;
import se.tink.backend.system.rpc.UpdateDocumentResponse;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class NewAgentTestContext extends AgentContext {
    private static final Logger log = LoggerFactory.getLogger(NewAgentTestContext.class);
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final Map<String, Account> accountsByBankId = new HashMap<>();
    private final Map<String, AccountFeatures> accountFeaturesByBankId = new HashMap<>();
    private final Map<String, List<Transaction>> transactionsByAccountBankId = new HashMap<>();
    private final Map<String, List<TransferDestinationPattern>> transferDestinationPatternsByAccountBankId = new HashMap<>();
    private final List<Transfer> transfers = new ArrayList<>();

    private final User user;
    private final Credentials credential;

    // configuration
    private final int transactionsToPrint;

    public NewAgentTestContext(User user, Credentials credential, int transactionsToPrint) {
        this.user = user;
        this.credential = credential;
        this.transactionsToPrint = transactionsToPrint;

        setTestContext(true);
        setAggregatorInfo(AggregatorInfo.getAggregatorForTesting());
    }

    @Override
    public void clear() {
        super.clear();

        accountsByBankId.clear();
        accountFeaturesByBankId.clear();
        transactionsByAccountBankId.clear();
        transferDestinationPatternsByAccountBankId.clear();
        transfers.clear();
    }

    public List<Account> getUpdatedAccounts() {
        return Lists.newArrayList(accountsByBankId.values());
    }

    public List<Transaction> getTransactions() {
        return transactionsByAccountBankId.entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public List<Transfer> getTransfers() {
        return transfers;
    }

    public List<TransferDestinationPattern> getTransferDestinationPatterns() {
        return transferDestinationPatternsByAccountBankId.entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Override
    public void processTransactions() {
        // noop
    }

    private void displaySupplementalInformation(Credentials credentials) {
        log.info("Requesting supplemental information.");

        List<Field> supplementalInformation = SerializationUtils.deserializeFromString(
                credentials.getSupplementalInformation(), new TypeReference<List<Field>>() {
                }
        );

        List<Map<String, String>> output = supplementalInformation.stream()
                .map(field -> {
                    Map<String, String> row = new LinkedHashMap<>();
                    row.put("name", field.getName());
                    row.put("description", field.getDescription());
                    row.put("helpText", field.getHelpText());
                    row.put("masked", String.valueOf(field.isMasked()));
                    row.put("sensitive", String.valueOf(field.isSensitive()));
                    return row;
                })
                .collect(Collectors.toList());
        CliPrintUtils.printTable(0, "supplemental information", output);
    }

    @Override
    public Optional<String> waitForSupplementalInformation(String key, long waitFor, TimeUnit unit) {
        return Optional.ofNullable(AgentTestServerClient.waitForSupplementalInformation(key, waitFor, unit));
    }

    @Override
    public String requestSupplementalInformation(Credentials credentials, boolean wait) {
        log.info("Requesting additional info from client. Status: {}, wait: {}", credentials.getStatus(), wait);

        switch (credentials.getStatus()) {
        case AWAITING_SUPPLEMENTAL_INFORMATION:
            displaySupplementalInformation(credentials);

            AgentTestServerClient.initiateSupplementalInformation(credentials.getId(),
                    credentials.getSupplementalInformation());

            if (!wait) {
                // The agent is not interested in the result. This is the same logic as the production code.
                return null;
            }

            Optional<String> supplementalInformation = waitForSupplementalInformation(credentials.getId(), 2,
                    TimeUnit.MINUTES);

            return supplementalInformation.orElse(null);
        case AWAITING_MOBILE_BANKID_AUTHENTICATION:
            // Do nothing as we cannot communicate to the app to open BankId.
            return null;
        case AWAITING_THIRD_PARTY_APP_AUTHENTICATION:
            AgentTestServerClient.openThirdPartyApp(credentials.getSupplementalInformation());
            return null;
        default:
            Assert.fail(String.format("Cannot handle credentials status: %s", credentials.getStatus()));
            return null;
        }
    }

    @Override
    public void openBankId(String autoStartToken, boolean wait) {
        if (Strings.isNullOrEmpty(autoStartToken)) {
            log.info(String.format("[CredentialsId:%s]: Open BankID", credential.getId()));
        } else {
            log.info(String.format("[CredentialsId:%s]: Open BankID with autoStartToken: %s",
                    credential.getId(), autoStartToken));
        }
    }

    public void processAccounts() {
        //noop
    }

    @Override
    public void cacheAccount(Account account, AccountFeatures accountFeatures) {
        accountsByBankId.put(account.getBankId(), account);
        accountFeaturesByBankId.put(account.getBankId(), accountFeatures);
    }

    public Optional<AccountFeatures> getAccountFeatures(final String uniqueAccountIdentifier) {
        return Optional.ofNullable(accountFeaturesByBankId.get(uniqueAccountIdentifier));
    }

    public Account sendAccountToUpdateService(String uniqueId) {
        return accountsByBankId.get(uniqueId);
    }

    @Override
    public void updateTransferDestinationPatterns(
            Map<Account, List<TransferDestinationPattern>> transferDestinationPatterns) {

        for (Account account : transferDestinationPatterns.keySet()) {
            if (transferDestinationPatternsByAccountBankId.containsKey(account.getBankId())) {
                transferDestinationPatternsByAccountBankId.get(
                        account.getBankId()).addAll(transferDestinationPatterns.get(account));
            } else {
                transferDestinationPatternsByAccountBankId.put(account.getBankId(),
                        transferDestinationPatterns.get(account));
            }
        }
    }

    @Override
    public void updateStatus(CredentialsStatus status) {
        log.info("Updating status: " + status.name());

        credential.setStatus(status);
    }

    @Override
    public void updateStatus(CredentialsStatus status, String statusPayload, boolean statusFromProvider) {
        log.info("Updating status: " + status.name() + " (" + statusPayload + ") - statusFromProvider: "
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

        transactionsByAccountBankId.put(updatedAccount.getBankId(), transactions);
        return updatedAccount;
    }

    @Override
    public void cacheTransactions(@Nonnull String accountUniqueId, List<Transaction> transactions) {
        Preconditions.checkNotNull(accountUniqueId); // Necessary until we make @Nonnull throw the exception
        transactionsByAccountBankId.put(accountUniqueId, transactions);
    }

    public void updateSignableOperation(SignableOperation operation) {
        log.info("Updating transfer status: "
                + operation.getStatus()
                + (Strings.isNullOrEmpty(operation.getStatusMessage()) ?
                "" :
                " (" + operation.getStatusMessage() + ")"));
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
    public void updateCredentialsExcludingSensitiveInformation(Credentials credentials, boolean doUpdateStatus) {
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
    public UpdateDocumentResponse updateDocument(DocumentContainer contianer) {
        // Not in scope for this test
        return UpdateDocumentResponse.createSuccessful(contianer.getIdentifier(), UUID.randomUUID(), "url");
    }

    public void processEinvoices() {
        // noop
    }

    public void processTransferDestinationPatterns() {
        // noop
    }

    public void validateFetchedData(AisValidator validator) {
        validator.validate(
                accountsByBankId.values(),
                transactionsByAccountBankId
                        .values()
                        .stream()
                        .collect(ArrayList::new, List::addAll, List::addAll));
    }

    private void printLoanDetails(List<Loan> loans) {
        List<Map<String, String>> table = loans
                .stream()
                .map(loan -> {
                    Map<String, String> row = new LinkedHashMap<>();

                    row.put("type",
                            Optional.ofNullable(loan.getType())
                                    .map(Loan.Type::name)
                                    .orElse(null)
                    );
                    row.put("number", loan.getLoanNumber());
                    row.put("name", loan.getName());
                    row.put("balance", String.valueOf(loan.getBalance()));
                    row.put("interest", String.valueOf(loan.getInterest()));

                    Optional<LoanDetails> details = Optional.ofNullable(loan.getLoanDetails());
                    row.put("co-applicants",
                            String.valueOf(
                                    details.map(LoanDetails::getCoApplicant)
                                            .orElse(null)
                            )
                    );
                    row.put("applicants",
                            String.valueOf(
                                    details.map(LoanDetails::getApplicants)
                                            .orElse(null)
                            )
                    );
                    row.put("security",
                            details.map(LoanDetails::getLoanSecurity)
                                    .orElse(null)
                    );
                    return row;
                })
                .collect(Collectors.toList());
        CliPrintUtils.printTable(4, "loans", table);
    }

    private void printInstrumentDetails(List<Instrument> instruments) {
        List<Map<String, String>> table = instruments.stream()
                .map(instrument -> {
                    Map<String, String> row = new LinkedHashMap<>();

                    row.put("uniqueId", instrument.getUniqueIdentifier());
                    row.put("type (raw type)",
                            String.format(
                                    "%s (%s)",
                                    instrument.getType().name(),
                                    instrument.getRawType()
                            )
                    );
                    row.put("isin", instrument.getIsin());
                    row.put("ticker", instrument.getTicker());
                    row.put("name", instrument.getName());
                    row.put("market", instrument.getMarketPlace());
                    row.put("price", String.valueOf(instrument.getPrice()));
                    row.put("value", String.valueOf(instrument.getMarketValue()));
                    row.put("profit", String.valueOf(instrument.getProfit()));
                    row.put("quantity", String.valueOf(instrument.getQuantity()));
                    row.put("currency", String.valueOf(instrument.getCurrency()));
                    row.put("aap", String.valueOf(instrument.getAverageAcquisitionPrice()));

                    return row;
                })
                .collect(Collectors.toList());
        CliPrintUtils.printTable(8, "instruments", table);
    }

    private void printPortfolioDetails(List<Portfolio> portfolios) {
        portfolios.forEach(portfolio -> {
            Map<String, String> row = new LinkedHashMap<>();

            row.put("uniqueId", portfolio.getUniqueIdentifier());
            row.put("type (raw type)",
                    String.format("%s (%s)", portfolio.getType().name(), portfolio.getRawType()));
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
        account.getIdentifiers().forEach(
                identifier -> {
                    Map<String, String> row = new LinkedHashMap<>();
                    row.put("name", identifier.getName().orElse(null));
                    row.put("type", identifier.getType().toString());
                    row.put("identifier", identifier.getIdentifier());
                    identifierList.add(row);
                }
        );
        CliPrintUtils.printTable(4, "identifiers", identifierList);
    }

    private void printAccountInformation(Account account) {
        Map<String, String> row = new LinkedHashMap<>();

        row.put("accountId", account.getBankId());
        row.put("type", account.getType().name());
        row.put("number", account.getAccountNumber());
        row.put("name", account.getName());
        row.put("balance", String.valueOf(account.getBalance()));
        CliPrintUtils.printTable(0, "account", Lists.newArrayList(row));

        printAccountIdentifiers(account);

        AccountFeatures accountFeatures = accountFeaturesByBankId.get(account.getBankId());

        switch (account.getType()) {
        case LOAN:
        case MORTGAGE:
            Assert.assertNotNull(accountFeatures.getLoans());
            Assert.assertFalse(accountFeatures.getLoans().isEmpty());
            printLoanDetails(accountFeatures.getLoans());
            break;

        case INVESTMENT:
            Assert.assertNotNull(accountFeatures.getPortfolios());
            for (Portfolio portfolio : accountFeatures.getPortfolios()) {
                Assert.assertNotNull(portfolio.getInstruments());
                Assert.assertFalse(portfolio.getInstruments().isEmpty());
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
        List<Map<String, String>> table = transactionsByAccountBankId.getOrDefault(bankId, Collections.emptyList())
                .stream()
                .sorted(Comparator.comparing(Transaction::getDate))
                .map(transaction -> {
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
        List<Map<String, String>> table = transferDestinationPatternsByAccountBankId
                .getOrDefault(bankId, Collections.emptyList())
                .stream()
                .map(transferDestination -> {
                    Map<String, String> row = new LinkedHashMap<>();
                    row.put("type",
                            Optional.ofNullable(transferDestination.getType())
                                    .map(AccountIdentifier.Type::name)
                                    .orElse(null)
                    );
                    row.put("bank", transferDestination.getBank());
                    row.put("name", transferDestination.getName());
                    row.put("pattern", transferDestination.getPattern());
                    return row;
                })
                .collect(Collectors.toList());
        CliPrintUtils.printTable(4, "transfer destinations", table);
    }

    private void printTransfers() {
        List<Map<String, String>> table = transfers.stream()
                .map(transfer -> {
                    Map<String, String> row = new LinkedHashMap<>();

                    row.put("id", transfer.getId().toString());
                    row.put("source",
                            Optional.ofNullable(transfer.getSource())
                                    .map(AccountIdentifier::toString)
                                    .orElse("")
                    );
                    row.put("destination",
                            Optional.ofNullable(transfer.getDestination())
                                    .map(AccountIdentifier::toString)
                                    .orElse("")
                    );
                    row.put("amount",
                            Optional.ofNullable(transfer.getAmount())
                                    .map(x -> String.format("%s %s", x.getValue(), x.getCurrency()))
                                    .orElse("")
                    );

                    return row;
                })
                .collect(Collectors.toList());

        CliPrintUtils.printTable(0, "transfers", table);
    }

    public void printCollectedData() {
        accountsByBankId.forEach((bankId, account) -> {
            printAccountInformation(account);
            printTransactions(bankId);
            printTransferDestinations(bankId);
            System.out.println("");
        });

        printTransfers();
    }

    public void printStatistics() {
        //
    }
}
