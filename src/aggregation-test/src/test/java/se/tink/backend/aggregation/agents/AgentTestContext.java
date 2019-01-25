package se.tink.backend.aggregation.agents;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.ws.rs.core.MediaType;
import org.apache.curator.framework.CuratorFramework;
import org.assertj.core.util.Preconditions;
import se.tink.backend.aggregation.api.AggregatorInfo;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.core.DocumentContainer;
import se.tink.backend.aggregation.agents.models.fraud.FraudDetailsContent;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.system.rpc.AccountFeatures;
import se.tink.backend.system.rpc.Transaction;
import se.tink.libraries.log.legacy.LogUtils;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.metrics.MetricRegistry;

public class AgentTestContext extends AgentContext {
    private static final LogUtils log = new LogUtils(AgentTestContext.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final TinkHttpClient supplementalClient = new TinkHttpClient(null, null);
    private static final String SUPPLEMENTAL_TEST_API = "http://127.0.0.1:7357/api/v1/supplemental";
    private static final String CLUSTER_ID_FOR_TESTING = "test-local-development";

    static {
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z"));

    }

    private Map<String, Account> accountsByBankId = Maps.newHashMap();
    private Map<String, List<Transaction>> transactionsByAccountBankId = Maps.newHashMap();
    private Map<Account, List<TransferDestinationPattern>> transferDestinationPatternsByAccount = Maps.newHashMap();
    private List<FraudDetailsContent> detailsContents;
    private List<Transfer> transfers = Lists.newArrayList();
    private Credentials credentials;

    public AgentTestContext(Credentials credentials) {
        this.credentials = credentials;

        setClusterId(CLUSTER_ID_FOR_TESTING);
        setAggregatorInfo(AggregatorInfo.getAggregatorForTesting());
    }

    @Override
    public void clear() {
        super.clear();
        accountsByBankId.clear();
        transactionsByAccountBankId.clear();
        transfers.clear();
    }

    public List<Account> getUpdatedAccounts() {
        return Lists.newArrayList(accountsByBankId.values());
    }

    public List<FraudDetailsContent> getDetailsContents() {
        return detailsContents;
    }

    public Map<String, List<Transaction>> getTransactionsByAccountBankId() {
        return transactionsByAccountBankId;
    }

    public List<Transaction> getTransactions() {
        List<Transaction> transactions = Lists.newArrayList();

        for (String accountId : transactionsByAccountBankId.keySet()) {
            transactions.addAll(transactionsByAccountBankId.get(accountId));
        }

        return transactions;
    }

    public List<Transfer> getTransfers() {
        return transfers;
    }

    @Override
    public Optional<String> waitForSupplementalInformation(String key, long waitFor, TimeUnit unit) {
        return Optional.empty();
    }

    @Override
    public void processTransactions() {
        log.info("Processing transactions");

        int numberOfAccounts = 0;
        int numberOfTransactions = 0;

        try {
            for (String bankId : accountsByBankId.keySet()) {
                numberOfAccounts++;

                log.info(mapper.writeValueAsString(accountsByBankId.get(bankId)));

                if (transactionsByAccountBankId.containsKey(bankId)) {
                    for (Transaction transaction : transactionsByAccountBankId.get(bankId)) {
                        log.info("\t" + mapper.writeValueAsString(transaction));
                        numberOfTransactions++;
                    }
                }

                log.info("");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("Processed " + numberOfAccounts + " accounts and " + numberOfTransactions + " transactions.");
    }

    @Override
    public String requestSupplementalInformation(Credentials credentials, boolean wait) {
        log.info("Requesting supplemental information:" + credentials.getStatus().name() + " ("
                + credentials.getSupplementalInformation() + ")");

        if (!wait) {
            return null;
        }

        return supplementalClient.request(SUPPLEMENTAL_TEST_API)
                .type(MediaType.APPLICATION_JSON)
                .post(String.class, credentials.getSupplementalInformation());
    }

    @Override
    public void openBankId(String autoStartToken, boolean wait) {
        if (Strings.isNullOrEmpty(autoStartToken)) {
            log.info(String.format("[CredentialsId:%s]: Open BankID", credentials.getId()));
        } else {
            log.info(String.format("[CredentialsId:%s]: Open BankID with autoStartToken: %s",
                    credentials.getId(), autoStartToken));
        }
    }

    public void processAccounts() {
        log.info("Processing accounts");
    }

    @Override
    public void cacheAccount(Account account, AccountFeatures accountFeatures) {
        log.info("Updating account");

        try {
            log.info(mapper.writeValueAsString(account));
            log.info(mapper.writeValueAsString(accountFeatures));
        } catch (Exception e) {
            // NOOP.
        }

        accountsByBankId.put(account.getBankId(), account);
    }

    public Optional<AccountFeatures> getAccountFeatures(final String uniqueAccountId) {
        throw new NotImplementedException("Account features need to be cached to be retrievable");
    }

    public Account sendAccountToUpdateService(String uniqueId) {
        return accountsByBankId.get(uniqueId);
    }

    @Override
    public void updateTransferDestinationPatterns(
            Map<Account, List<TransferDestinationPattern>> transferDestinationPatterns) {
        log.info("Updating transfer destination patterns");

        for (Account account : transferDestinationPatterns.keySet()) {
            if (transferDestinationPatternsByAccount.containsKey(account)) {
                transferDestinationPatternsByAccount.get(account).addAll(transferDestinationPatterns.get(account));
            } else {
                transferDestinationPatternsByAccount.put(account, transferDestinationPatterns.get(account));
            }

            log.info("\t" + account.toString());

            for (TransferDestinationPattern pattern : transferDestinationPatterns.get(account)) {
                log.info("\t\t" + pattern.toString());
            }
        }
    }

    @Override
    public void updateStatus(CredentialsStatus status) {
        log.info("Updating status: " + status.name());

        credentials.setStatus(status);
    }

    @Override
    public void updateStatus(CredentialsStatus status, String statusPayload, boolean statusFromProvider) {
        log.info("Updating status: " + status.name() + " (" + statusPayload + ")");

        credentials.setStatus(status);
        credentials.setStatusPayload(statusPayload);
    }

    @Override
    @Deprecated // Use cacheTransactions instead
    public Account updateTransactions(Account account, List<Transaction> transactions) {
        try {
            log.info("Updating transactions for account: " + mapper.writeValueAsString(account));
        } catch (Exception e) {
        }

        cacheAccount(account);
        account = sendAccountToUpdateService(account.getBankId());

        for (Transaction updatedTransaction : transactions) {
            updatedTransaction.setAccountId(account.getId());
            updatedTransaction.setCredentialsId(account.getCredentialsId());
            updatedTransaction.setUserId(account.getUserId());
        }

        transactionsByAccountBankId.put(account.getBankId(), transactions);

        return account;
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
        detailsContents = contents;
    }

    @Override
    public Catalog getCatalog() {
        return Catalog.getCatalog("en_US");
    }

    @Override
    public void updateCredentialsExcludingSensitiveInformation(Credentials credentials, boolean doUpdateStatus) {
        // nothing
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

        if (transfers.size() > 0) {
            log.info("Updating transfers");

            for (Transfer transfer : transfers) {
                try {
                    log.info(mapper.writeValueAsString(transfer));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void updateDocument(DocumentContainer contianer) {
        log.info(
                "-------------------------------------------------------DOCUMENT---------------------------------------------------------------------");
        log.info(String.format("Identifier: %s", contianer.getIdentifier()));
        log.info(
                "-------------------------------------------------------DOCUMENT---------------------------------------------------------------------");
    }

    public void processEinvoices() {
        try {
            log.info(
                    "------------------------------------------------------------TRANSFERS--------------------------------------------------------------------------");
            log.info(mapper.writeValueAsString(transfers));
            log.info(
                    "------------------------------------------------------------TRANSFERS--------------------------------------------------------------------------");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void processTransferDestinationPatterns() {
        try {
            log.info(
                    "------------------------------------------------------------TRANSFER DESTINATIONS----------------------------------------------------------------");
            log.info(mapper.writeValueAsString(transferDestinationPatternsByAccount));
            log.info(
                    "------------------------------------------------------------TRANSFER DESTINATIONS----------------------------------------------------------------");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
