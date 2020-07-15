package se.tink.backend.aggregation.agents.framework.context;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.ws.rs.core.MediaType;
import org.apache.curator.framework.CuratorFramework;
import org.assertj.core.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountHolder;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.agents.models.fraud.FraudDetailsContent;
import se.tink.backend.aggregation.api.AggregatorInfo;
import se.tink.backend.aggregation.fakelogmasker.FakeLogMasker;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl.LoggingMode;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.libraries.account_data_cache.AccountDataCache;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.transfer.rpc.Transfer;

public class AgentTestContext extends AgentContext {

    private static final String SUPPLEMENTAL_TEST_API = "http://127.0.0.1:7357/api/v1/supplemental";
    private static final String CLUSTER_ID_FOR_TESTING = "test-local-development";

    private final Logger log;
    private final ObjectMapper mapper;
    private final TinkHttpClient supplementalClient;

    private final AccountDataCache accountDataCache;
    private Map<String, Account> accountsByBankId = Maps.newHashMap();
    private Map<String, List<Transaction>> transactionsByAccountBankId = Maps.newHashMap();
    private Map<Account, List<TransferDestinationPattern>> transferDestinationPatternsByAccount =
            Maps.newHashMap();
    private List<FraudDetailsContent> detailsContents;
    private List<Transfer> transfers = Lists.newArrayList();
    private Credentials credentials;
    private String providerSessionCacheInformation;

    public AgentTestContext(Credentials credentials) {
        LogMasker logMasker = new FakeLogMasker();
        setLogMasker(logMasker);
        supplementalClient =
                NextGenTinkHttpClient.builder(logMasker, LoggingMode.LOGGING_MASKER_COVERS_SECRETS)
                        .build();
        log = LoggerFactory.getLogger(AgentTestContext.class);
        mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z"));

        this.credentials = credentials;
        this.accountDataCache = new AccountDataCache();

        setClusterId(CLUSTER_ID_FOR_TESTING);
        setAggregatorInfo(AggregatorInfo.getAggregatorForTesting());
    }

    @Override
    public void clear() {
        super.clear();
        accountDataCache.clear();
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
    public Optional<String> waitForSupplementalInformation(
            String key, long waitFor, TimeUnit unit) {
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

        log.info(
                "Processed "
                        + numberOfAccounts
                        + " accounts and "
                        + numberOfTransactions
                        + " transactions.");
    }

    @Override
    public String requestSupplementalInformation(Credentials credentials, boolean wait) {
        log.info(
                "Requesting supplemental information:"
                        + credentials.getStatus().name()
                        + " ("
                        + credentials.getSupplementalInformation()
                        + ")");

        if (!wait) {
            return null;
        }

        return supplementalClient
                .request(SUPPLEMENTAL_TEST_API)
                .type(MediaType.APPLICATION_JSON)
                .post(String.class, credentials.getSupplementalInformation());
    }

    @Override
    public void openBankId(String autoStartToken, boolean wait) {
        if (Strings.isNullOrEmpty(autoStartToken)) {
            log.info(String.format("[CredentialsId:%s]: Open BankID", credentials.getId()));
        } else {
            log.info(
                    String.format(
                            "[CredentialsId:%s]: Open BankID with autoStartToken: %s",
                            credentials.getId(), autoStartToken));
        }
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

        accountDataCache.cacheAccount(account);
        accountDataCache.cacheAccountFeatures(account.getBankId(), accountFeatures);
    }

    @Override
    public void cacheIdentityData(IdentityData identityData) {
        log.info("Updating identity data");

        try {
            log.info(mapper.writeValueAsString(identityData));
        } catch (Exception e) {
            // NOOP.
        }
    }

    public Account sendAccountToUpdateService(String uniqueId) {
        return accountsByBankId.get(uniqueId);
    }

    @Override
    public AccountHolder sendAccountHolderToUpdateService(String tinkId) {
        return accountsByBankId.values().stream()
                .filter(a -> Objects.equals(tinkId, a.getId()))
                .findFirst()
                .map(Account::getAccountHolder)
                .orElse(null);
    }

    public Account updateAccount(String uniqueId) {
        return accountsByBankId.get(uniqueId);
    }

    @Override
    public void updateTransferDestinationPatterns(
            Map<Account, List<TransferDestinationPattern>> transferDestinationPatterns) {
        log.info("Updating transfer destination patterns");

        transferDestinationPatterns.forEach(
                (account, patterns) ->
                        accountDataCache.cacheTransferDestinationPatterns(
                                account.getBankId(), patterns));

        for (Account account : transferDestinationPatterns.keySet()) {
            if (transferDestinationPatternsByAccount.containsKey(account)) {
                transferDestinationPatternsByAccount
                        .get(account)
                        .addAll(transferDestinationPatterns.get(account));
            } else {
                transferDestinationPatternsByAccount.put(
                        account, transferDestinationPatterns.get(account));
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
    public void updateStatus(
            CredentialsStatus status, String statusPayload, boolean statusFromProvider) {
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

        cacheTransactions(account.getBankId(), transactions);
        return account;
    }

    @Override
    public void cacheTransactions(@Nonnull String accountUniqueId, List<Transaction> transactions) {
        Preconditions.checkNotNull(
                accountUniqueId); // Necessary until we make @Nonnull throw the exception
        transactionsByAccountBankId.put(accountUniqueId, transactions);
        accountDataCache.cacheTransactions(accountUniqueId, transactions);
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
    public void updateCredentialsExcludingSensitiveInformation(
            Credentials credentials, boolean doUpdateStatus) {
        // nothing
    }

    @Override
    public void updateCredentialsExcludingSensitiveInformation(
            Credentials credentials, boolean doUpdateStatus, boolean isMigrationUpdate) {
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
    public void sendIdentityToIdentityAggregatorService() {
        // TODO: implement sending identity data
        throw new NotImplementedException("Method not implemented");
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

    @Override
    public String getProviderSessionCache() {
        log.info("Getting provider session cache information");
        return this.providerSessionCacheInformation;
    }

    @Override
    public void setProviderSessionCache(String value, int expiredTimeInSeconds) {
        log.info("Setting provider session cache information");
        this.providerSessionCacheInformation = value;
    }
}
