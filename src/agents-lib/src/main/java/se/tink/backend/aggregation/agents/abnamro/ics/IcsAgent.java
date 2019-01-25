package se.tink.backend.aggregation.agents.abnamro.ics;

import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.FeatureFlags;
import se.tink.backend.aggregation.agents.abnamro.utils.AbnAmroIcsCredentials;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.RefreshableItemExecutor;
import se.tink.backend.aggregation.agents.abnamro.ics.mappers.AccountMapper;
import se.tink.backend.aggregation.agents.abnamro.ics.mappers.TransactionMapper;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.RefreshableItem;
import se.tink.backend.agents.rpc.User;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.agents.abnamro.ics.retry.RetryerBuilder;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.libraries.log.legacy.LogUtils;
import se.tink.libraries.abnamro.client.IBSubscriptionClient;
import se.tink.libraries.abnamro.client.exceptions.IcsRetryableException;
import se.tink.libraries.abnamro.client.model.creditcards.CreditCardAccountContainerEntity;
import se.tink.libraries.abnamro.client.model.creditcards.CreditCardAccountEntity;
import se.tink.libraries.abnamro.client.model.creditcards.TransactionContainerEntity;
import se.tink.libraries.abnamro.config.AbnAmroConfiguration;
import se.tink.libraries.abnamro.utils.AbnAmroUtils;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.metrics.MetricRegistry;


/**
 * Agent for collection credit cards transactions towards ABN AMRO / ICS (International Card Services).
 * <p/>
 * - This agent are connecting to the same endpoint and with the same authentication token as we use when communicating
 *   with ABN AMRO for subscriptions, authentication etc.
 * - We can not retrieve accounts from this endpoint so these are stored as a payload field on the credentials. New
 *   accounts cannot be added.
 * - This agent can and should only be used by ABN AMRO.
 */
public class IcsAgent extends AbstractAgent implements RefreshableItemExecutor {
    private final Credentials credentials;
    private final MetricRegistry metricRegistry;
    private IBSubscriptionClient ibSubscriptionClient;
    private User user;
    private Retryer<List<CreditCardAccountContainerEntity>> retryer;
    private boolean hasRefreshed = false;

    public IcsAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context);

        this.credentials = request.getCredentials();
        this.user = request.getUser();
        this.metricRegistry = context.getMetricRegistry();

        retryer = RetryerBuilder.<List<CreditCardAccountContainerEntity>>newBuilder(new LogUtils(IcsAgent.class),
                "fetching ics transactions")
                .withStopStrategy(StopStrategies.stopAfterAttempt(2))
                .withWaitStrategy(WaitStrategies.fixedWait(100, TimeUnit.MILLISECONDS))
                .retryIfExceptionOfType(IcsRetryableException.class)
                .build();
    }

    @Override
    public boolean login() throws AuthenticationException, AuthorizationException {
        return true; // We can always login since we are using a static token as authentication towards ABN AMRO
    }

    @Override
    public void logout() throws Exception {
        // NOP
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        AbnAmroConfiguration abnAmroConfiguration = getValidAbnAmroConfiguration(configuration);

        try {
            this.ibSubscriptionClient = new IBSubscriptionClient(abnAmroConfiguration, metricRegistry);
        } catch (Exception e) {
            throw new RuntimeException("Could not instantiate Internet Banking Client", e);
        }
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
    public void refresh(RefreshableItem item) {
        if (hasRefreshed) {
            return;
        }
        hasRefreshed = true;

        AbnAmroIcsCredentials abnAmroIcsCredentials = new AbnAmroIcsCredentials(credentials);

        String bcNumber = abnAmroIcsCredentials.getBcNumber();
        Set<Long> contractNumbers = abnAmroIcsCredentials.getContractNumbers();

        Preconditions.checkNotNull(bacNumber);

        if (contractNumbers.isEmpty()) {
            return;
        }

        updateAccountsAndTransactions(bcNumber, contractNumbers);
    }

    /**
     * The ICS API support refresh of multiple accounts/contracts at the same time but if one fails then it fails for
     * all of them and we don't want that.
     * @param bcNumber Bank Customer Number (The number that we identify the user with at ABN AMRO)
     * @param contractNumbers A set of contracts for which we should try to fetch transactions
     */
    private void updateAccountsAndTransactions(String bcNumber, Set<Long> contractNumbers) {

        ErrorMessageBuilder errorMessageBuilder = new ErrorMessageBuilder(Catalog.getCatalog(user.getProfile().getLocale()));

        for (Long contractNumber : contractNumbers) {
            try {
                updateAccountAndTransactions(bcNumber, contractNumber);
            } catch (Exception e) {
                log.error("Error when calling ABN AMRO credit card service", e);
                errorMessageBuilder.addException(contractNumber, e);
            }
        }

        if (errorMessageBuilder.hasExceptions()) {
            statusUpdater.updateStatus(CredentialsStatus.TEMPORARY_ERROR, errorMessageBuilder.build());
        }
    }

    public static boolean shouldUseNewIcsAccountFormat(List<String> featureFlags) {
        return featureFlags.contains(FeatureFlags.ABN_AMRO_ICS_NEW_ACCOUNT_FORMAT);
    }

    /**
     * Retrieve transactions for a specific contract that belongs to a customer (Bank Customer Number)
     */
    private void updateAccountAndTransactions(String bcNumber, Long contractNumber) throws Exception {
        List<CreditCardAccountContainerEntity> entities = retryer
                .call(() -> ibSubscriptionClient.getCreditCardAccountAndTransactions(bcNumber, contractNumber));

        for (CreditCardAccountContainerEntity entity : entities) {
            CreditCardAccountEntity creditCardAccount = entity.getCreditCardAccount();

            Account account = AccountMapper.toAccount(creditCardAccount,
                    shouldUseNewIcsAccountFormat(user.getFlags()));

            List<Transaction> transactions = creditCardAccount.getTransactions().stream()
                    .filter(TransactionContainerEntity::isInEUR)
                    .map(TransactionMapper::toTransaction)
                    .collect(Collectors.toList());

            financialDataCacher.updateTransactions(account, transactions);
        }
    }
}
