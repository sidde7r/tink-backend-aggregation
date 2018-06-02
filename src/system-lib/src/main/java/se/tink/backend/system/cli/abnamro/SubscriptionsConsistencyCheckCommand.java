package se.tink.backend.system.cli.abnamro;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import java.util.Optional;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.AbnAmroSubscriptionRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.AbnAmroSubscription;
import se.tink.backend.core.Account;
import se.tink.backend.core.User;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.cli.helper.traversal.CommandLineInterfaceUserTraverser;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.abnamro.client.IBSubscriptionClient;
import se.tink.libraries.abnamro.client.model.InactiveContractEntity;
import se.tink.libraries.abnamro.client.rpc.SubscriptionResponse;
import se.tink.libraries.abnamro.config.AbnAmroConfiguration;
import se.tink.libraries.abnamro.utils.AbnAmroAccountCompareUtils;
import se.tink.libraries.abnamro.utils.AbnAmroLegacyUserUtils;
import se.tink.libraries.abnamro.utils.AbnAmroUtils;
import se.tink.libraries.metrics.MetricRegistry;

/**
 * Utility command that goes through all customers in the Tink database and compare them against the information
 * that is available at ABN AMRO. Useful since it will detect data consistency issues. The command also update the
 * information in the Tink database if for example we have an account marked as inactive but it is active at ABN AMRO.
 */
public class SubscriptionsConsistencyCheckCommand extends ServiceContextCommand<ServiceConfiguration> {

    private static final LogUtils log = new LogUtils(SubscriptionsConsistencyCheckCommand.class);

    private AccountRepository accountRepository;
    private AbnAmroSubscriptionRepository abnAmroSubscriptionRepository;
    private IBSubscriptionClient subscriptionClient;

    public SubscriptionsConsistencyCheckCommand() {
        super("abnamro-subscription-consistency-check", "Update and compare customer subscription and accounts.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        if (!Objects.equal(configuration.getCluster(), Cluster.ABNAMRO)) {
            log.error("This command is only enabled in the ABN AMRO cluster.");
            return;
        }

        accountRepository = injector.getInstance(AccountRepository.class);
        abnAmroSubscriptionRepository = injector.getInstance(AbnAmroSubscriptionRepository.class);
        subscriptionClient = new IBSubscriptionClient(injector.getInstance(AbnAmroConfiguration.class),
                injector.getInstance(MetricRegistry.class));

        injector.getInstance(UserRepository.class).streamAll()
                .compose(new CommandLineInterfaceUserTraverser(1))
                .forEach(user -> {
                    try {
                        process(user);
                    } catch (Exception e) {
                        log.error(user.getId(), "Processing failed.", e);
                    }
                });
    }

    private void process(User user) throws Exception {
        if (user.getFlags().contains(FeatureFlags.TINK_TEST_ACCOUNT)) {
            log.warn(user.getId(), "Don't process users with test flag. Skipping.");
            return;
        }

        Optional<String> bcNumber = AbnAmroLegacyUserUtils.getBcNumber(user);

        if (!bcNumber.isPresent()) {
            log.error(user.getId(), "Don't process users without bc-number. Skipping.");
            return;
        }

        AbnAmroSubscription subscription = abnAmroSubscriptionRepository.findOneByUserId(user.getId());

        if (subscription == null || !subscription.isActivated()) {
            log.info(user.getId(), "Don't process users without active subscription. Skipping.");
            return;
        }

        Optional<SubscriptionResponse> abnSubscription = subscriptionClient.getSubscription(bcNumber.get());

        if (!abnSubscription.isPresent()) {
            log.error(user.getId(), "Could not retrieve subscription information from ABN AMRO.");
            return;
        }

        processUser(user, subscription, abnSubscription.get());

        processAccounts(user, accountRepository.findByUserId(user.getId()), abnSubscription.get());
    }

    private void processUser(User user, AbnAmroSubscription our, SubscriptionResponse their) {
        String customer = AbnAmroLegacyUserUtils.getBcNumber(user).orElse("N/A");

        if (our.isActivated() && !their.isCustomerActive()) {
            log.error(user.getId(), String.format(
                    "Customer is active at Tink but inactive at ABN AMRO (Customer = '%s', Status = '%s', Code = '%s').",
                    customer, their.getCustomerStatus(), their.getStatus()));
        } else {
            log.info(user.getId(), "User status in sync.");
        }

        // Log if the user is active but have inactive accounts. This should not happen.
        if (their.isCustomerActive() && !their.getInactiveContracts().isEmpty()) {
            for (InactiveContractEntity entity : their.getInactiveContracts()) {
                log.warn(user.getId(), String.format(
                        "Customer is active but account inactive (Customer = '%s', Account = '%s', Reason = '%s', Code = '%s').",
                        customer, entity.getContractNumber(), entity.getStatusReason(), entity.getStatus()));
            }
        }

        // Log if the customer isn't active but have active accounts. This should not happen.
        if (!their.isCustomerActive() && !their.getActiveContracts().isEmpty()) {
            log.error(user.getId(), String.format(
                    "Customer is not active but some accounts are active. (Customer = '%s', Reason = '%s', Code = '%s').",
                    their.getBcNumber(), their.getCustomerStatus(), their.getStatus()));
        }
    }

    private void processAccounts(User user, List<Account> accounts, SubscriptionResponse subscription) {
        ImmutableMap<String, Account> accountByBankId = Maps.uniqueIndex(accounts, Account::getBankId);

        ImmutableMap<String, InactiveContractEntity> inactiveAccountsByBankId = Maps
                .uniqueIndex(subscription.getInactiveContracts(), InactiveContractEntity::getContractNumber);

        AbnAmroAccountCompareUtils comparer = new AbnAmroAccountCompareUtils(accounts,
                subscription.getActiveContracts(), inactiveAccountsByBankId.keySet());

        AbnAmroAccountCompareUtils.Result result = comparer.compare();

        // Log that all accounts are in sync
        if (result.isValid()) {
            log.info(user.getId(), "All accounts are in sync.");
            return;
        }

        String customer = AbnAmroLegacyUserUtils.getBcNumber(user).orElse("N/A");

        // Log the account numbers that are active at Tink but doesn't exist at ABN AMRO.
        for (String accountNumber : result.getMissingAtAbnAmro()) {
            log.warn(user.getId(),
                    String.format("Account is missing at Abn Amro (Customer = '%s', Account = '%s').",
                            customer, accountNumber));
        }

        // Log the account numbers that are active at ABN AMRO but doesn't exist at TINK.
        for (String accountNumber : result.getMissingAtTink()) {
            log.warn(user.getId(),
                    String.format("Account is missing at Tink (Customer = '%s', Account = '%s').", customer,
                            accountNumber));
        }

        for (String accountNumber : result.getActiveAtAbnAmroInactiveAtTink()) {
            log.warn(user.getId(), String.format(
                    "Account is active at Abn Amro but inactive at Tink. Updating account to active (Customer = '%s', Account = '%s').",
                    customer, accountNumber));

            Account account = accountByBankId.get(accountNumber);

            if (account != null) {
                AbnAmroUtils.markAccountAsActive(account);
                accountRepository.save(account);
            }
        }

        for (String accountNumber : result.getActiveAtTinkInactiveAtAbnAmro()) {
            log.warn(user.getId(), String.format(
                    "Account is active at Tink but inactive at Abn Amro. Updating account to inactive (Customer = '%s', Account = '%s').",
                    customer, accountNumber));

            Account account = accountByBankId.get(accountNumber);
            InactiveContractEntity entity = inactiveAccountsByBankId.get(accountNumber);

            if (account != null && entity != null) {
                AbnAmroUtils.markAccountAsRejected(account, entity.getStatus());
                accountRepository.save(account);
            }
        }
    }
}
