package se.tink.backend.combined.load;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.google.common.io.Files;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.curator.framework.CuratorFramework;
import com.google.common.collect.Lists;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.system.rpc.Transaction;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.backend.system.rpc.AccountFeatures;
import se.tink.backend.core.DocumentContainer;
import se.tink.backend.core.FraudDetailsContent;
import se.tink.backend.core.account.TransferDestinationPattern;
import se.tink.backend.core.application.ApplicationState;
import se.tink.backend.core.product.ProductPropertyKey;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.system.rpc.UpdateDocumentResponse;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class AgentRegressionTestContext extends AgentContext {

    private static final LogUtils log = new LogUtils(AgentRegressionTestContext.class);
    private File outFile = null;
    private String runName;
    private int accountCount = 0;
    private int transactionsCount = 0;
    private List<Transaction> transactions = Lists.newArrayList();
    private Map<String, Account> accounts = Maps.newHashMap();
    private boolean formatDate = false;

    protected CredentialsStatus status = CredentialsStatus.CREATED;

    protected static final Ordering<Account> ACCOUNT_ORDERING = new Ordering<Account>() {
        @Override
        public int compare(Account left, Account right) {
            return left.getBankId().compareTo(right.getBankId());
        }
    };

    protected static final Ordering<Transaction> TRANSACTION_ORDERING = new Ordering<Transaction>() {
        @Override
        public int compare(Transaction left, Transaction right) {
            return ComparisonChain.start().compare(left.getDate(), right.getDate())
                    .compare(left.getDescription(), right.getDescription())
                    .compare(left.getAmount(), right.getAmount()).result();
        }
    };

    public AgentRegressionTestContext(String runName) {
        this.runName = runName;
    }

    public AgentRegressionTestContext(String runName, boolean formatDate) {
        this.runName = runName;
        this.formatDate = formatDate;
    }

    @Override
    public Account updateAccount(Account account, AccountFeatures accountFeatures) {
        if (!accounts.containsKey(account.getBankId())) {
            accounts.put(account.getBankId(), account);
        }

        return account;
    }

    @Override
    public void updateTransferDestinationPatterns(Map<Account, List<TransferDestinationPattern>> map) {
    }

    @Override
    public Account updateTransactions(Account account, List<Transaction> transactionsList) {
        // use account number as sorted and index since they are same for both runs
        if (!accounts.containsKey(account.getBankId())) {
            accounts.put(account.getBankId(), account);
        }

        for (Transaction t : transactionsList) {
            t.setAccountId(account.getBankId());
            transactions.add(t);
        }

        return account;
    }

    public void processAccounts() {

    }

    @Override
    public void processTransactions() {
        try {
            if (outFile == null) {
                createOutFile();
            }

            for (Account a : accounts.values()) {
                if (a.getAccountNumber() == null) {
                    a.setAccountNumber(a.getBankId());
                }
            }

            ImmutableMap<String, Account> accountsById = Maps.uniqueIndex(
                    ACCOUNT_ORDERING.sortedCopy(accounts.values()), Account::getBankId);

            ImmutableListMultimap<String, Transaction> transactionsByAccountId = Multimaps.index(
                    TRANSACTION_ORDERING.sortedCopy(transactions), Transaction::getAccountId);

            for (String accountId : accountsById.keySet()) {
                Files.append("--- ACCOUNT ---" + "\n", outFile, Charsets.UTF_8);
                accountCount++;
                Files.append(getComparableAccountText(accountsById.get(accountId)), outFile, Charsets.UTF_8);
                Files.append("--- TRANSACTIONS ---" + "\n", outFile, Charsets.UTF_8);
                for (Transaction t : transactionsByAccountId.get(accountId)) {
                    Files.append(getComparableTransactionText(t), outFile, Charsets.UTF_8);
                    transactionsCount++;
                }
            }

            String summary = "Processed " + accountCount + " accounts and " + transactionsCount + " transactions";
            Files.append(summary + "\n", outFile, Charsets.UTF_8);
            log.info(summary);

        } catch (IOException e) {
            log.error("Could not write to " + outFile, e);
        }
    }

    private void createOutFile() {
        try {
            outFile = new File(System.getProperty("user.dir") +
                    "/data/test/tink-backend-agent-tests/" + runName + ".txt");
            Files.createParentDirs(outFile);
            Files.write("", outFile, Charsets.UTF_8);

        } catch (IOException e) {
            log.error("Could not write out file for run: " + runName, e);
        }
    }

    private String getComparableTransactionText(Transaction t) {
        String date = t.getDate().toString();
        if (formatDate) {
            date = ThreadSafeDateFormat.FORMATTER_SECONDS.format(t.getDate());
        }

        String description = t.getDescription();
        if (t.getOriginalDescription() != null) {
            description = t.getOriginalDescription();
        }

        double amount = t.getAmount();
        if (t.getOriginalAmount() != 0) {
            amount = t.getOriginalAmount();
        }

        return date + "\t" + description.toUpperCase() + "\t" + amount + "\n";
    }

    private String getComparableAccountText(Account a) {
        return a.getBankId() + "\t" + a.getAccountNumber() + "\t" + a.getName() + "\t" + a.getBalance() + "\n";
    }

    @Override
    public Catalog getCatalog() {
        return Catalog.getCatalog("en_US");
    }

    @Override
    public String requestSupplementalInformation(Credentials credentials, boolean wait) {
        log.info("Requesting supplemental information:" + status.name() + " ("
                + credentials.getSupplementalInformation() + ")");

        if (!wait) {
            return null;
        }

        log.info("Please enter supplemental information (and hit enter):");

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            String info = reader.readLine();

            return info;
        } catch (Exception e) {
            log.error("Caught exception while waiting for supplemental information", e);
            return null;
        }
    }

    @Override
    public void openBankId(String autoStartToken, boolean wait) {
        if (Strings.isNullOrEmpty(autoStartToken)) {
            log.info("Open BankID");
        } else {
            log.info(String.format("Open BankID with autoStartToken: %s", autoStartToken));
        }
    }

    @Override
    public void updateStatus(CredentialsStatus status) {
        log.info("Updating status: " + status.name());
        this.status = status;
    }

    @Override
    public void updateStatus(CredentialsStatus status, String statusPayload, boolean statusFromProvider) {
        log.info("Updating status: " + status.name() + " (" + statusPayload + ")");
        this.status = status;
    }

    @Override
    public void clear() {
        // nothing
    }

    @Override
    public void updateSignableOperation(SignableOperation operation) {

    }

    public CredentialsStatus getStatus() {
        return status;
    }

    @Override
    public void updateCredentialsExcludingSensitiveInformation(Credentials credentials) {
        // nothing
    }

    @Override
    public void updateCredentialsOnlySensitiveInformation(Credentials credentials) {
        // nothing
    }

    @Override
    public boolean isCredentialDeleted(String credentialsId) {
        return false;
    }

    @Override
    public void updateFraudDetailsContent(List<FraudDetailsContent> detailsContent) {
        // nothing
    }

    @Override
    public CuratorFramework getCoordinationClient() {
        // nothing
        return null;
    }

    @Override
    public MetricRegistry getMetricRegistry() {
        return new MetricRegistry();
    }

    @Override
    public List<Account> getAccounts() {
        return Lists.newArrayList(accounts.values());
    }

    @Override
    public void updateEinvoices(List<Transfer> transfers) {
        // nothing
    }

    @Override
    public void processTransferDestinationPatterns() {
        // nothing

    }

    @Override
    public void processEinvoices() {
        // nothing
    }

    @Override
    public void updateProductInformation(UUID productInstanceId,
            HashMap<ProductPropertyKey, Object> productProperties) {
        // nothing
    }

    @Override
    public void updateApplication(UUID applicationId, ApplicationState applicationState) {
        // nothing
    }

    @Override
    public UpdateDocumentResponse updateDocument(DocumentContainer container) {
        // nothing
        return null;
    }
}
