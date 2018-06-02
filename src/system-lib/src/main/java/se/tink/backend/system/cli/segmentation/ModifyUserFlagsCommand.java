package se.tink.backend.system.cli.segmentation;

import com.google.common.base.Preconditions;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import java.util.function.Consumer;
import net.sourceforge.argparse4j.inf.Namespace;
import rx.Observable;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.common.utils.repository.SubsetPredicate;
import se.tink.backend.core.User;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.cli.helper.traversal.CommandLineInterfaceUserTraverser;
import se.tink.backend.system.cli.helper.traversal.CommandLineUserFilterFactory;

public class ModifyUserFlagsCommand extends ServiceContextCommand {
    private final static LogUtils log = new LogUtils(ModifyUserFlagsCommand.class);
    private UserRepository userRepository;
    private SubsetPredicate<User> subsetFilter;

    public ModifyUserFlagsCommand() {
        super("modify-user-flag", "Command for adding/removing/modifying feature flags for users");
    }

    private static enum Task {
        ADD_FLAG,
        REMOVE_FLAG
    }

    @Override
    protected void run(Bootstrap bootstrap, Namespace namespace, ServiceConfiguration configuration, Injector injector,
            ServiceContext serviceContext) throws Exception {
        userRepository = serviceContext.getRepository(UserRepository.class);
        try {
            String flag = Preconditions.checkNotNull(
                    System.getProperty("flag"), "flag-argument must bu supplied");
            Task task = Task.valueOf(Preconditions.checkNotNull(
                    System.getProperty("task"), "task-argument must be supplied"));
            CommandLineInterfaceUserTraverser traverser = new CommandLineInterfaceUserTraverser(20);
            double subsetSizeRatio = 1.0d;

            /**
             * subsetSizeRatio should only be set when traversing all users,
             * not when using the userIds file or userId/username properties
             * See {@link CommandLineUserFilterFactory.subsetSizePreconditions }
             * */
            if (traverser.checkSubsetSizeRatio()) {
                subsetSizeRatio = Double.valueOf(Preconditions
                        .checkNotNull(System.getProperty("subsetSizeRatio"), "subsetSizeRatio must be supplied"));
            }
            subsetFilter = new SubsetPredicate<User>(subsetSizeRatio, (int) userRepository.count());
            Observable<User> users = userRepository
                    .streamAll()
                    .compose(traverser);
            switch (task) {
            case ADD_FLAG:
                modifyUserFlag(users, flag, true);
                log.info(String.format("Added flags for %d users", subsetFilter.count()));
                break;
            case REMOVE_FLAG:
                modifyUserFlag(users, flag, false);
                log.info(String.format("removed flags for %d users", subsetFilter.count()));
                break;
            }
        } catch (Exception e) {
            log.info("Exception occurred when modifying flags", e);
        }
    }

    private void modifyUserFlag(final Observable<User> users, String flag, boolean add) {
        // Note about ^ / boolean XOR:
        // If we are adding a flag (task=ADD_FLAG) then u.getFlags().contains should return False
        // since we only want to add to users without the flag (True XOR False = True).
        // If we are removing a flag (task=REMOVE_FLAG) then u.getFlags().contains should return True
        // since we only want to remove the flag from users that have it (False XOR True = True).
        Consumer<User> consumer = (user) -> {
            List<String> userFlags = user.getFlags();
            if (add) {
                userFlags.add(flag);
            } else {
                userFlags.remove(flag);
            }
            user.setFlags(userFlags);
            userRepository.save(user);
        };
        users.filter(u -> add ^ u.getFlags().contains(flag)).filter(subsetFilter::test).forEach(consumer::accept);
    }
}
