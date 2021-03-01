package se.tink.backend.aggregation.agents.framework.context;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
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
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.api.AggregatorInfo;
import se.tink.backend.aggregation.fakelogmasker.FakeLogMasker;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl.LoggingMode;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.connectivity.errors.ConnectivityError;
import se.tink.libraries.account_data_cache.AccountData;
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

    public AccountDataCache getAccountDataCache() {
        return accountDataCache;
    }

    public List<Account> getUpdatedAccounts() {
        return Lists.newArrayList(accountDataCache.getProcessedAccounts());
    }

    public List<Transaction> getTransactions() {
        return accountDataCache.getTransactionsToBeProcessed();
    }

    public List<Transfer> getTransfers() {
        return transfers;
    }

    @Override
    public Optional<String> waitForSupplementalInformation(
            String mfaId, long waitFor, TimeUnit unit, String initiator) {
        return Optional.ofNullable(
                supplementalClient
                        .request(SUPPLEMENTAL_TEST_API)
                        .type(MediaType.APPLICATION_JSON)
                        .post(String.class, credentials.getSupplementalInformation()));
    }

    @Override
    public void processTransactions() {
        log.info("Processing transactions");

        accountDataCache
                .getTransactionsByAccountToBeProcessed()
                .forEach(
                        (account, transactions) -> {
                            try {
                                log.info(mapper.writeValueAsString(account));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            transactions.forEach(
                                    transaction -> {
                                        try {
                                            log.info("\t" + mapper.writeValueAsString(transaction));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    });
                            log.info("");
                        });

        log.info(
                "Processed "
                        + accountDataCache.getProcessedAccounts().size()
                        + " accounts and "
                        + accountDataCache.getTransactionsToBeProcessed().size()
                        + " transactions.");
    }

    @Override
    public void requestSupplementalInformation(String mfaId, Credentials credentials) {
        log.info(
                "Requesting supplemental information:"
                        + credentials.getStatus().name()
                        + " ("
                        + credentials.getSupplementalInformation()
                        + ")");
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

        accountDataCache.cacheAccount(account);
        accountDataCache.cacheAccountFeatures(account.getBankId(), accountFeatures);

        // Automatically process it when cached. This is because we don't have a WorkerCommand doing
        // this for us.
        sendAccountToUpdateService(account.getBankId());
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
        log.info("Updating transfer destination patterns");

        transferDestinationPatterns.forEach(
                (account, patterns) -> {
                    accountDataCache.cacheAccount(account);
                    accountDataCache.cacheTransferDestinationPatterns(
                            account.getBankId(), patterns);

                    log.info("\t" + account.toString());
                    patterns.forEach(pattern -> log.info("\t\t" + pattern.toString()));
                });
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
    public void updateStatusWithError(
            CredentialsStatus status, String statusPayload, ConnectivityError error) {
        log.info(
                "Updating status: {}, also having error: {}",
                status.name(),
                error.getType().name());

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
        return Catalog.getCatalog("en_US");
    }

    @Override
    public void updateCredentialsExcludingSensitiveInformation(
            Credentials credentials, boolean doUpdateStatus) {
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
            log.info(
                    mapper.writeValueAsString(
                            accountDataCache.getTransferDestinationPatternsToBeProcessed()));
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
