package se.tink.backend.system.cli.segmentation;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import net.sourceforge.argparse4j.inf.Namespace;
import org.eclipse.jetty.util.BlockingArrayQueue;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.common.concurrency.TypedThreadPoolBuilder;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.utils.RandomSample;
import se.tink.backend.core.Market;
import se.tink.backend.core.User;
import se.tink.backend.system.api.UpdateService;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.utils.LogUtils;

public class DistributeUserFlagCommand extends ServiceContextCommand<ServiceConfiguration> {

    private static final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .build();

    private static class HasFlag implements Predicate<User> {

        private final String flag;
        private boolean isTestFlag;

        public HasFlag(String flag) {
            this.flag = flag;
            this.isTestFlag = flag.startsWith("TEST_");
        }

        @Override
        public boolean apply(User user) {
            if (isTestFlag) {
                return user.getFlags().contains(flag + ON_SUFFIX);
            } else {
                return user.getFlags().contains(flag);
            }
        }
    }

    private static final String modifyPreexistingFlagsetFlag = "forceModification";
    private static final String ON_SUFFIX = "_ON";
    private static final String OFF_SUFFIX = "_OFF";
    private static ListenableThreadPoolExecutor<Runnable> executor;
    private static BlockingQueue<Runnable> queue = new BlockingArrayQueue<>();

    private static final LogUtils log = new LogUtils(DistributeUserFlagCommand.class);

    public DistributeUserFlagCommand() {
        super("distribute-user-flag", "Makes sure a given subset of users has a user flag.");
    }

    private void addFlag(final UpdateService updateService, final String flag, List<User> toUsers) {
        for (final User user : toUsers) {
            executor.execute(() -> {
                List<String> userFlags = user.getFlags();
                Preconditions.checkArgument(!userFlags.contains(flag), "Expected every user not to have the flag.");

                userFlags.add(flag);
                log.info(user.getId(),
                        String.format("Adding %s to user with username: %s", flag, user.getUsername()));

                updateService.updateUserFlags(user.getId(), userFlags);
            });
        }
    }

    private void deleteFlag(final UpdateService updateService, final String flag, List<User> fromUsers) {
        for (final User user : fromUsers) {
            executor.execute(() -> {
                List<String> userFlags = user.getFlags();
                Preconditions.checkArgument(userFlags.contains(flag), "Expected every user to have the flag.");

                userFlags.remove(flag);
                log.info(user.getId(),
                        String.format("Removing %s from user with username: %s", flag, user.getUsername()));

                updateService.updateUserFlags(user.getId(), userFlags);
            });
        }
    }

    private void turnOffFlag(final UpdateService updateService, final String flag, List<User> fromUsers) {
        for (final User user : fromUsers) {
            executor.execute(() -> {
                List<String> userFlags = user.getFlags();

                Preconditions.checkArgument(userFlags.contains(flag + ON_SUFFIX),
                        "Expected every user to have the flag.");

                userFlags.remove(flag + ON_SUFFIX);
                userFlags.add(flag + OFF_SUFFIX);

                updateService.updateUserFlags(user.getId(), userFlags);
            });
        }
    }

    private void setOffFlag(final UpdateService updateService, final User user, final String flag) {
        executor.execute(() -> {
            List<String> userFlags = user.getFlags();

            userFlags.remove(flag + ON_SUFFIX);
            userFlags.add(flag + OFF_SUFFIX);

            updateService.updateUserFlags(user.getId(), userFlags);
        });
    }

    private void setOnFlag(final UpdateService updateService, final User user, final String flag) {
        executor.execute(() -> {
            List<String> userFlags = user.getFlags();

            userFlags.remove(flag + OFF_SUFFIX);
            userFlags.add(flag + ON_SUFFIX);

            updateService.updateUserFlags(user.getId(), userFlags);
        });
    }

    /**
     * Modify user flags.
     *
     *
     * @param updateService
     * @param userRepository
     * @param requestedNumberOfUsers
     * @param flag
     * @param allowFlagCreation
     * @param isTestFlag
     * @param userIdWhitelist
     * @throws IOException
     * @throws InterruptedException
     * @note Limitation: Does not handle removal of flags given to users outside of userIdWhitelist, if one such is a
     * given.
     */
    private void modifyUsers(UpdateService updateService, UserRepository userRepository,
            int requestedNumberOfUsers, String flag,
            boolean allowFlagCreation, boolean isTestFlag, final Optional<Set<String>> userIdWhitelist,
            final Optional<String> market) throws InterruptedException {
        Preconditions.checkNotNull(flag);
        Preconditions.checkNotNull(updateService);
        Preconditions.checkNotNull(userRepository);

        HasFlag flagChecker = new HasFlag(flag);

        List<User> users = userRepository.findAll();
        FluentIterable<User> filteredUsers = FluentIterable.from(users);

        // Default and if user whitelisting is provided, we use all users as base
        FluentIterable<User> usersWithFlagBase = FluentIterable.from(users);

        if (userIdWhitelist.isPresent()) {
            filteredUsers = filteredUsers.filter(filterOnUserId(userIdWhitelist.get()));
        } else if (market.isPresent()) {
            filteredUsers = filteredUsers.filter(filterOnMarket(market.get()));
            // market restriction uses only users on the restricted market as base
            usersWithFlagBase = FluentIterable.from(filteredUsers.toList());
        }

        ArrayList<User> usersWithFlag = Lists.newArrayList(usersWithFlagBase.filter(flagChecker).toList());
        Iterable<User> usersAllowedToHaveFlag = filteredUsers.toList();

        Preconditions.checkArgument(usersWithFlag.size() == 0 || allowFlagCreation,
                "The flag already exists. Define '-D" + DistributeUserFlagCommand.modifyPreexistingFlagsetFlag
                        + "=true' to allow modifying a preexisting flagset.");

        if (usersWithFlag.size() > requestedNumberOfUsers) {
            log.info("Too many users with the flag. Removing the flag from some of them.");

            // Select a random subset of users that should have the flag deleted/OFF.

            List<User> userWithFlags = RandomSample.from(usersWithFlag)
                    .pick(usersWithFlag.size() - requestedNumberOfUsers);

            if (isTestFlag) {
                turnOffFlag(updateService, flag, userWithFlags);
            } else {
                deleteFlag(updateService, flag, userWithFlags);
            }

        } else if (usersWithFlag.size() < requestedNumberOfUsers) {

            log.info("Too few users with the flag. Adding the flag from some of those who are missing it.");

            // Select a random subset of users that should have the flag/ON.

            ArrayList<User> usersWithoutFlag = Lists
                    .newArrayList(Iterables.filter(usersAllowedToHaveFlag, Predicates.not(flagChecker)));

            List<User> userSample = RandomSample.from(usersWithoutFlag)
                    .pick(requestedNumberOfUsers - usersWithFlag.size());

            // If test flag, set the selected users with "flag + _ON" and the rest with "flag + _OFF"

            if (isTestFlag) {

                Set<String> userSampleIds = Sets
                        .newHashSet(Iterables.transform(userSample, User::getId));

                // Set the ON flag for users in list.

                for (User u : users) {

                    if (userSampleIds.contains(u.getId())) {
                        setOnFlag(updateService, u, flag);
                    } else {

                        // If this is a run, dont change the ON flags.

                        if (!u.getFlags().contains(flag + ON_SUFFIX)) {
                            setOffFlag(updateService, u, flag);
                        }
                    }
                }
            } else {
                addFlag(updateService, flag, userSample);
            }

        } else {
            log.info("Number of users with the flag is as expected. No modification done.");
        }

        // Shut down

        executor.shutdown();
        while (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
            log.info(String.format("%d thread remaining to be started.", queue.size()));
        }
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        final UserRepository userRepository = serviceContext.getRepository(UserRepository.class);
        UpdateService updateService = injector.getInstance(SystemServiceFactory.class).getUpdateService();

        /**
         * If user id whitelisting is provided, market is disregarded.
         */

        String whitelistFile = System.getProperty("restrictFile");

        final String flag = System.getProperty("flag");
        Preconditions.checkArgument(flag != null, "'flag' parameter must be given.");
        Preconditions.checkArgument(flag.trim().length() > 0, "'flag' parameter can't have length 0.");
        log.info("Feature: " + flag);

        final String requestedNumberOfUsersString = System.getProperty("subsetSize");
        Preconditions.checkArgument(requestedNumberOfUsersString != null, "'subsetSize' parameter must be set.");

        final Integer nThreads = Integer.getInteger("threadPoolSize", 5);

        executor = ListenableThreadPoolExecutor.builder(
                Queues.newLinkedBlockingQueue(),
                new TypedThreadPoolBuilder(nThreads, threadFactory))
                .build();

        final Optional<String> market = Optional.ofNullable(System.getProperty("market", null));
        if (market.isPresent()) {
            if (whitelistFile != null) {
                throw new IllegalArgumentException("Cannot run with both restrictFile and market supplied.");
            }
            // market is optional but should be correct if set
            Preconditions.checkArgument(Market.Code.valueOf(market.get()) != null);
        }

        int requestedNumberOfUsers;

        if (requestedNumberOfUsersString.endsWith("%")) {
            String percentageString = requestedNumberOfUsersString.substring(0,
                    requestedNumberOfUsersString.length() - 1);
            Integer percentage = Integer.valueOf(percentageString);
            Preconditions.checkArgument(percentage >= 0, "Incorrect percentage.");
            Preconditions.checkArgument(percentage <= 100, "Incorrect percentage.");

            // TODO: Handle.
            Preconditions.checkArgument(whitelistFile == null,
                    "Whitelist file not compatible with relative flag distribution.");

            int userCount;
            if (market.isPresent()) {
                userCount = (int) userRepository.countByProfileMarket(market.get());
            } else {
                userCount = (int) userRepository.count();
            }

            requestedNumberOfUsers = (percentage * userCount) / 100;
        } else {
            requestedNumberOfUsers = Integer.valueOf(requestedNumberOfUsersString);
        }
        log.info("Number of users getting the flag: " + requestedNumberOfUsers);

        boolean modifyPreexistingFlagset = Boolean.getBoolean(modifyPreexistingFlagsetFlag);
        boolean isTestFlag = flag.startsWith("TEST_");

        // A restriction file is useful if not all users are candidates for the flag. The file contains a userid on
        // each line. Nothing else.
        Optional<Set<String>> userIdWhitelist = Optional.empty();
        if (whitelistFile != null) {
            File userIdFilterFile = new File(whitelistFile);
            Set<String> allowedUserIds = Sets.newHashSet(Files.readLines(userIdFilterFile, Charsets.UTF_8));
            userIdWhitelist = Optional.of(allowedUserIds);
        }

        modifyUsers(updateService, userRepository, requestedNumberOfUsers, flag, modifyPreexistingFlagset,
                isTestFlag, userIdWhitelist, market);
    }

    private Predicate<User> filterOnMarket(final String market) {
        return user -> market.equals(user.getProfile().getMarket());
    }

    private Predicate<User> filterOnUserId(final Set<String> whiteListedUserids) {
        return user -> whiteListedUserids.contains(user.getId());
    }
}
