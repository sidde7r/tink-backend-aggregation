package se.tink.backend.aggregation.agents.abnamro.ics;

import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.assertj.core.util.Lists;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.FeatureFlags;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.abnamro.client.IBSubscriptionClient;
import se.tink.backend.aggregation.agents.abnamro.client.exceptions.IcsRetryableException;
import se.tink.backend.aggregation.agents.abnamro.client.model.creditcards.CreditCardAccountContainerEntity;
import se.tink.backend.aggregation.agents.abnamro.client.model.creditcards.CreditCardAccountEntity;
import se.tink.backend.aggregation.agents.abnamro.client.model.creditcards.TransactionContainerEntity;
import se.tink.backend.aggregation.agents.abnamro.ics.mappers.AccountMapper;
import se.tink.backend.aggregation.agents.abnamro.ics.mappers.TransactionMapper;
import se.tink.backend.aggregation.agents.abnamro.ics.retry.RetryerBuilder;
import se.tink.backend.aggregation.agents.abnamro.utils.AbnAmroIcsCredentials;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.integrations.abnamro.AbnAmroConfiguration;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.user.rpc.User;

/**
 * Agent for collection credit cards transactions towards ABN AMRO / ICS (International Card
 * Services).
 *
 * <p>- This agent are connecting to the same endpoint and with the same authentication token as we
 * use when communicating with ABN AMRO for subscriptions, authentication etc. - We can not retrieve
 * accounts from this endpoint so these are stored as a payload field on the credentials. New
 * accounts cannot be added. - This agent can and should only be used by ABN AMRO.
 */
@AgentCapabilities(generateFromImplementedExecutors = true)
public final class IcsAgent extends AbstractAgent implements RefreshCreditCardAccountsExecutor {

    private final Credentials credentials;
    private final MetricRegistry metricRegistry;
    private IBSubscriptionClient ibSubscriptionClient;
    private User user;
    private Retryer<List<CreditCardAccountContainerEntity>> retryer;

    // cache
    private Map<Account, List<Transaction>> accounts = new HashMap<>();

    public IcsAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context);

        this.credentials = request.getCredentials();
        this.user = request.getUser();
        this.metricRegistry = context.getMetricRegistry();

        retryer =
                RetryerBuilder.<List<CreditCardAccountContainerEntity>>newBuilder(
                                LoggerFactory.getLogger(IcsAgent.class),
                                "fetching ics transactions")
                        .withStopStrategy(StopStrategies.stopAfterAttempt(2))
                        .withWaitStrategy(WaitStrategies.fixedWait(100, TimeUnit.MILLISECONDS))
                        .retryIfExceptionOfType(IcsRetryableException.class)
                        .build();
    }

    @Override
    public boolean login() throws AuthenticationException, AuthorizationException {
        return true; // We can always login since we are using a static token as authentication
        // towards
        // ABN AMRO
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
            this.ibSubscriptionClient =
                    new IBSubscriptionClient(abnAmroConfiguration, metricRegistry);
        } catch (Exception e) {
            throw new RuntimeException("Could not instantiate Internet Banking Client", e);
        }
    }

    private AbnAmroConfiguration getValidAbnAmroConfiguration(
            AgentsServiceConfiguration configuration) {
        if (Objects.nonNull(configuration.getAbnAmro())) {
            return configuration.getAbnAmro();
        }

        String clusterIdentifier =
                Optional.ofNullable(context.getClusterId())
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Failed to fetch cluster identifier."));

        switch (clusterIdentifier.toLowerCase()) {
            case "leeds-staging":
                return configuration.getAbnAmroStaging();
            case "leeds-production":
                return configuration.getAbnAmroProduction();
            default:
                throw new IllegalStateException("This agent can only be used by Leeds cluster.");
        }
    }

    /**
     * The ICS API support refresh of multiple accounts/contracts at the same time but if one fails
     * then it fails for all of them and we don't want that.
     *
     * @param bcNumber Bank Customer Number (The number that we identify the user with at ABN AMRO)
     * @param contractNumbers A set of contracts for which we should try to fetch transactions
     */
    private Map<Account, List<Transaction>> getAccountDetails(
            String bcNumber, Set<Long> contractNumbers) {

        ErrorMessageBuilder errorMessageBuilder =
                new ErrorMessageBuilder(Catalog.getCatalog(user.getProfile().getLocale()));

        for (Long contractNumber : contractNumbers) {
            try {
                return updateAccountAndTransactions(bcNumber, contractNumber);
            } catch (Exception e) {
                log.error("Error when calling ABN AMRO credit card service", e);
                errorMessageBuilder.addException(contractNumber, e);
            }
        }

        if (errorMessageBuilder.hasExceptions()) {
            statusUpdater.updateStatus(
                    CredentialsStatus.TEMPORARY_ERROR, errorMessageBuilder.build());
        }
        return Collections.EMPTY_MAP;
    }

    /**
     * Retrieve transactions for a specific contract that belongs to a customer (Bank Customer
     * Number)
     */
    private Map<Account, List<Transaction>> updateAccountAndTransactions(
            String bcNumber, Long contractNumber) throws Exception {
        List<CreditCardAccountContainerEntity> entities =
                retryer.call(
                        () ->
                                ibSubscriptionClient.getCreditCardAccountAndTransactions(
                                        bcNumber, contractNumber));

        Map<Account, List<Transaction>> result = new HashMap<>();

        for (CreditCardAccountContainerEntity entity : entities) {
            CreditCardAccountEntity creditCardAccount = entity.getCreditCardAccount();

            Account account =
                    AccountMapper.toAccount(
                            creditCardAccount, shouldUseNewIcsAccountFormat(user.getFlags()));
            List<Transaction> transactions =
                    creditCardAccount.getTransactions().stream()
                            .filter(TransactionContainerEntity::isInEUR)
                            .map(TransactionMapper::toTransaction)
                            .collect(Collectors.toList());

            result.put(account, transactions);
        }
        return result;
    }

    public static boolean shouldUseNewIcsAccountFormat(List<String> featureFlags) {
        return featureFlags.contains(FeatureFlags.ABN_AMRO_ICS_NEW_ACCOUNT_FORMAT);
    }

    private Map<Account, List<Transaction>> getAccounts(
            String bcNumber, Set<Long> contractNumbers) {
        return getAccountDetails(bcNumber, contractNumbers);
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        AbnAmroIcsCredentials abnAmroIcsCredentials = new AbnAmroIcsCredentials(credentials);

        String bcNumber = abnAmroIcsCredentials.getBcNumber();
        Set<Long> contractNumbers = abnAmroIcsCredentials.getContractNumbers();

        Preconditions.checkNotNull(bcNumber);

        if (contractNumbers.isEmpty()) {
            return new FetchAccountsResponse(Lists.emptyList());
        }

        this.accounts = getAccounts(bcNumber, contractNumbers);

        return new FetchAccountsResponse(new ArrayList<>(this.accounts.keySet()));
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return new FetchTransactionsResponse(this.accounts);
    }
}
