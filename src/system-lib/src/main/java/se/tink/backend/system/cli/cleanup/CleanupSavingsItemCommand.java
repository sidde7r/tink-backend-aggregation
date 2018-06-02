package se.tink.backend.system.cli.cleanup;

import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.FollowItemRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.User;
import se.tink.backend.core.follow.FollowItem;
import se.tink.backend.core.follow.FollowTypes;
import se.tink.backend.core.follow.SavingsFollowCriteria;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.cli.helper.traversal.CommandLineInterfaceUserTraverser;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CleanupSavingsItemCommand extends ServiceContextCommand<ServiceConfiguration> {

    public static final LogUtils log = new LogUtils(CleanupSavingsItemCommand.class);
    private AccountRepository accountRepository;
    private FollowItemRepository followRepository;

    public CleanupSavingsItemCommand() {
        super("cleanup-savings-items", "Cleanup savings items");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        final boolean dryRun = Boolean.getBoolean("dryRun");

        accountRepository = injector.getInstance(AccountRepository.class);
        followRepository = injector.getInstance(FollowItemRepository.class);
        UserRepository userRepository = injector.getInstance(UserRepository.class);

        final AtomicInteger affectedItemCount = new AtomicInteger();

        log.info(String.format("Start cleanup savings items , dryRun=%b.", dryRun));

        userRepository.streamAll()
                .compose(new CommandLineInterfaceUserTraverser(10))
                .forEach(user -> {
                    try {
                        affectedItemCount.getAndSet(cleanup(injector, dryRun, user));
                    } catch (Exception e) {
                        log.error(user.getId(), "Failed to cleanup savings items", e);
                    }
                });

        log.info(String.format("Processed savings items: %s.", affectedItemCount.get()));
    }

    private int cleanup(Injector injector, boolean dryRun, User user) {
        Set<String> userAccountIds = accountRepository.findByUserId(user.getId()).stream()
                .map(Account::getId)
                .collect(Collectors.toSet());

        List<FollowItem> affectedSavings = followRepository.findByUserId(user.getId()).stream()
                .filter(followItem -> followItem.getType().equals(FollowTypes.SAVINGS))
                .peek(followItem -> followItem.setFollowCriteria(SerializationUtils.deserializeFromString(
                        followItem.getCriteria(), SavingsFollowCriteria.class)))
                .filter(followItem -> !userAccountIds
                        .containsAll(((SavingsFollowCriteria) followItem.getFollowCriteria()).getAccountIds()))
                .collect(Collectors.toList());

        if (!dryRun) {
            updateFollowItems(affectedSavings, userAccountIds);
        }

        return affectedSavings.size();
    }

    private void updateFollowItems(List<FollowItem> followItems, Set<String> userAccountIds) {
        for (FollowItem followItem : followItems) {
            SavingsFollowCriteria followCriteria = (SavingsFollowCriteria) followItem.getFollowCriteria();
            List<String> remainingAccountIds = followCriteria.getAccountIds().stream()
                    .filter(userAccountIds::contains)
                    .collect(Collectors.toList());
            if (remainingAccountIds.isEmpty()) {
                followRepository.delete(followItem.getId());
            } else {
                followCriteria.setAccountIds(remainingAccountIds);
                followItem.setFollowCriteria(followCriteria);
                followRepository.save(followItem);
            }
        }
    }
}
