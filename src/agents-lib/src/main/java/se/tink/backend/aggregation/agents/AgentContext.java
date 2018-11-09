package se.tink.backend.aggregation.agents;

import com.google.common.collect.Maps;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.curator.framework.CuratorFramework;
import se.tink.backend.aggregation.api.AggregatorInfo;
import se.tink.backend.aggregation.aggregationcontroller.v1.core.HostConfiguration;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.core.DocumentContainer;
import se.tink.backend.core.FraudDetailsContent;
import se.tink.backend.core.account.TransferDestinationPattern;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.system.rpc.AccountFeatures;
import se.tink.backend.system.rpc.Transaction;
import se.tink.backend.system.rpc.UpdateDocumentResponse;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.metrics.MetricRegistry;

public abstract class AgentContext {
    private Map<String, Integer> transactionCountByEnabledAccount = Maps.newHashMap();
    protected ByteArrayOutputStream logOutputStream = new ByteArrayOutputStream();
    protected boolean isTestContext = false;
    private boolean isWaitingOnConnectorTransactions = false;
    private AggregatorInfo aggregatorInfo;
    private HostConfiguration hostConfiguration;
    public abstract Catalog getCatalog();

    public abstract CuratorFramework getCoordinationClient();

    public ByteArrayOutputStream getLogOutputStream() {
        return logOutputStream;
    }

    public abstract MetricRegistry getMetricRegistry();

    public HostConfiguration getHostConfiguration(){
        return hostConfiguration;
    }

    public AggregatorInfo getAggregatorInfo() {
        return aggregatorInfo;
    }

    public void setHostConfiguration(HostConfiguration hostConfiguration) {
        this.hostConfiguration = hostConfiguration;
    }

    public void setAggregator(AggregatorInfo aggregatorInfo) {
        this.aggregatorInfo = aggregatorInfo;
    }

    private String formatCredentialsStatusPayloadSuffix(long numberOfAccounts, long numberOfTransactions,
            Catalog catalog) {
        StringBuilder builder = new StringBuilder();

        builder.append(Catalog.format(catalog.getPluralString("{0} account", "{0} accounts", numberOfAccounts),
                numberOfAccounts));

        if (numberOfTransactions > 0) {
            builder.append(" ");
            builder.append(catalog.getString("and"));
            builder.append(" ");

            builder.append(Catalog.format(
                    catalog.getPluralString("{0} transaction", "{0} transactions", numberOfTransactions),
                    numberOfTransactions));
        }

        return builder.toString();
    }

    protected String createStatusPayload() {
        Catalog catalog = getCatalog();

        int numberOfAccounts = transactionCountByEnabledAccount.size();
        int numberOfTransactions = 0;

        for (Integer accountTransactions : transactionCountByEnabledAccount.values()) {
            numberOfTransactions += accountTransactions;
        }

        return Catalog
                .format(catalog.getString("Updating {0}..."), formatCredentialsStatusPayloadSuffix(
                        numberOfAccounts, numberOfTransactions, catalog));
    }

    public abstract void processAccounts();

    public abstract void processTransactions();

    public abstract void processTransferDestinationPatterns();

    public abstract void processEinvoices();

    public abstract Optional<String> waitForSupplementalInformation(String key, long waitFor, TimeUnit unit);

    public String requestSupplementalInformation(Credentials credentials) {
        return requestSupplementalInformation(credentials, true);
    }

    public abstract String requestSupplementalInformation(Credentials credentials, boolean wait);

    public void openBankId() {
        openBankId(null, false);
    }

    public abstract void openBankId(String autoStartToken, boolean wait);

    public void cacheAccount(Account account) {
        cacheAccount(account, AccountFeatures.createEmpty());
    }

    public void cacheAccounts(Iterable<Account> accounts) {
        for (Account account : accounts) {
            cacheAccount(account);
        }
    }

    public abstract Account sendAccountToUpdateService(String uniqueId);

    /**
     * Caches {@code account} and the associated {@code accountFeatures}, making {@code accountFeatures} retrievable
     * via {@code AgentContext::getAccountFeatures} after this method has been executed.
     *
     * @param account An account
     * @param accountFeatures A set of account features associated with said account
     */
    public abstract void cacheAccount(Account account, AccountFeatures accountFeatures);

    /**
     * @param uniqueAccountId Unique identifier for an account
     * @return The cached account features of the related account, or Optional.empty if the account is not cached
     */
    public abstract Optional<AccountFeatures> getAccountFeatures(String uniqueAccountId);

    public abstract void updateTransferDestinationPatterns(Map<Account, List<TransferDestinationPattern>> map);

    public abstract void updateStatus(CredentialsStatus status);

    public void updateStatus(CredentialsStatus status, Account account, List<Transaction> transactions) {

        if (account.isExcluded()) {
            if (transactionCountByEnabledAccount.containsKey(account.getBankId())) {
                transactionCountByEnabledAccount.remove(account.getBankId());
            }
        } else {
            transactionCountByEnabledAccount.put(account.getBankId(), transactions.size());
        }

        updateStatus(status, createStatusPayload());
    }

    public void updateStatus(CredentialsStatus status, String statusPayload) {
        updateStatus(status, statusPayload, true);
    }

    public abstract void updateStatus(CredentialsStatus status, String statusPayload, boolean statusFromProvider);

    @Deprecated // Use cacheTransactions instead
    public abstract Account updateTransactions(Account account, List<Transaction> transactions);

    public abstract void cacheTransactions(String accountUniqueId, List<Transaction> transactions);

    public abstract void updateCredentialsExcludingSensitiveInformation(Credentials credentials,
            boolean doStatusUpdate);

    public abstract void updateFraudDetailsContent(List<FraudDetailsContent> detailsContent);

    public void clear() {
        transactionCountByEnabledAccount.clear();
    }

    public boolean isTestContext() {
        return isTestContext;
    }

    public void setTestContext(boolean isTestContext) {
        this.isTestContext = isTestContext;
    }

    public void updateSignableOperationStatus(SignableOperation signableOperation, SignableOperationStatuses status) {
        signableOperation.setStatus(status);
        signableOperation.setStatusMessage(null);

        updateSignableOperation(signableOperation);
    }

    public void updateSignableOperationStatus(SignableOperation signableOperation, SignableOperationStatuses status,
            String statusMessage) {
        signableOperation.setStatus(status);
        signableOperation.setStatusMessage(statusMessage);

        updateSignableOperation(signableOperation);
    }

    public abstract void updateSignableOperation(SignableOperation signableOperation);

    public abstract UpdateDocumentResponse updateDocument(DocumentContainer container);

    public abstract List<Account> getUpdatedAccounts();

    public abstract void updateEinvoices(List<Transfer> transfers);

    public boolean isWaitingOnConnectorTransactions() {
        return isWaitingOnConnectorTransactions;
    }

    public void setWaitingOnConnectorTransactions(boolean waitingOnConnectorTransactions) {
        isWaitingOnConnectorTransactions = waitingOnConnectorTransactions;
    }
}
