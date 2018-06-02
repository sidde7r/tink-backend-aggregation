package se.tink.backend.system.cli.migration;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableListMultimap;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import net.sourceforge.argparse4j.inf.Namespace;
import org.springframework.dao.DataIntegrityViolationException;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.Field;
import se.tink.backend.core.Market;
import se.tink.backend.core.User;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.cli.helper.traversal.CommandLineInterfaceUserTraverser;
import se.tink.backend.utils.LogUtils;

public class MigrateNationalIdCommand extends ServiceContextCommand<ServiceConfiguration> {

    private static final LogUtils log = new LogUtils(MigrateNationalIdCommand.class);

    public MigrateNationalIdCommand() {
        super("migrate-national-id", "");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        final boolean dryRun = Boolean.getBoolean("dryRun");

        UserRepository userRepository = serviceContext.getRepository(UserRepository.class);
        CredentialsRepository credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);

        final Helper helper = new Helper(userRepository, credentialsRepository);
        final ImmutableListMultimap<String, String> userIdsByNationalId = helper.getUserIdsByNationalId();

        final AtomicInteger usersMigrated = new AtomicInteger();
        final AtomicInteger usersAlreadyMigrated = new AtomicInteger();
        final AtomicInteger usersWithUndeterminedIdentity = new AtomicInteger();
        final AtomicInteger usersWithConflictingIdentity = new AtomicInteger();

        userRepository.streamAll()
                .compose(new CommandLineInterfaceUserTraverser(10))
                .forEach(u -> {
                    // Sweden only
                    if (!Objects.equals(u.getProfile().getMarketAsCode(), Market.Code.SE)) {
                        return;
                    }

                    if (!Strings.isNullOrEmpty(u.getNationalId())) {
                        usersAlreadyMigrated.incrementAndGet();
                        log.debug(u.getId(), "User already has national id defined.");
                        return;
                    }

                    Optional<String> nationalId = helper.getCandidateNationalId(u);

                    if (!nationalId.isPresent()) {
                        usersWithUndeterminedIdentity.incrementAndGet();
                        log.debug(u.getId(), "User doesn't have a determined national id.");
                        return;
                    }

                    List<String> nationalIds = userIdsByNationalId.get(nationalId.get());

                    if (nationalIds == null || nationalIds.isEmpty()) {
                        usersWithUndeterminedIdentity.incrementAndGet();
                        log.error(u.getId(), "User has candidate for national id, but not in the global records. This should never happen. Something is wrong in the code.");
                    } else if (nationalIds.size() == 1) {
                        if (dryRun) {
                            log.info(u.getId(), "User has a unique national id candidate.");
                        } else {
                            log.debug(u.getId(), "User has a unique national id candidate. MIGRATE!");
                            helper.setNationalId(u, nationalId.get());
                        }
                        usersMigrated.incrementAndGet();
                    } else if (nationalIds.size() > 1) {
                        usersWithConflictingIdentity.incrementAndGet();
                        log.debug(u.getId(), "CONFLICT. Other users aspire for the same national id.");
                    }
                });

        int userCount =
                usersMigrated.intValue() + usersAlreadyMigrated.intValue() + usersWithUndeterminedIdentity.intValue()
                        + usersWithConflictingIdentity.intValue();

        log.info(String.format("Users included: %s", userCount));
        log.info(String.format("Users migrated: %s (%.2f)", usersMigrated.intValue(),
                divide(usersMigrated.intValue(), userCount)));
        log.info(String.format("Users already migrated: %s (%.2f)", usersAlreadyMigrated.intValue(),
                divide(usersAlreadyMigrated.intValue(), userCount)));
        log.info(String.format("Users with undetermined identity: %s (%.2f)", usersWithUndeterminedIdentity.intValue(),
                divide(usersWithUndeterminedIdentity.intValue(), userCount)));
        log.info(String.format("Users with conflicting identity: %s (%.2f)", usersWithConflictingIdentity.intValue(),
                divide(usersWithConflictingIdentity.intValue(), userCount)));
    }

    private static double divide(int numerator, int denominator) {
        if (denominator == 0) {
            return 0;
        }

        return (double) numerator / (double) denominator;
    }

    class Helper {
        private final LogUtils log = new LogUtils(Helper.class);

        private final UserRepository userRepository;
        private final CredentialsRepository credentialsRepository;

        public Helper(UserRepository userRepository, CredentialsRepository credentialsRepository) {
            this.userRepository = userRepository;
            this.credentialsRepository = credentialsRepository;
        }

        public void setNationalId(User user, String nationalId) {
            user.setNationalId(nationalId);

            try {
                userRepository.save(user);
            } catch (DataIntegrityViolationException e) {
                log.warn(user.getId(), "Unable to persist national id.", e);
            }
        }

        public ImmutableListMultimap<String, String> getUserIdsByNationalId() {
            final ImmutableListMultimap.Builder<String, String> userIdsByNationalIdBuilder = ImmutableListMultimap.builder();

            userRepository.streamAll()
                    .forEach(u -> {
                        // Sweden only
                        if (!Objects.equals(u.getProfile().getMarketAsCode(), Market.Code.SE)) {
                            return;
                        }

                        Optional<String> nationalId;

                        if (Strings.isNullOrEmpty(u.getNationalId())) {
                            nationalId = getCandidateNationalId(u);
                        } else {
                            nationalId = Optional.of(u.getNationalId());
                        }

                        if (nationalId.isPresent()) {
                            userIdsByNationalIdBuilder.put(nationalId.get(), u.getId());
                        }
                    });

            return userIdsByNationalIdBuilder.build();
        }

        public Optional<String> getCandidateNationalId(User user) {
            if (!Strings.isNullOrEmpty(user.getProfile().getFraudPersonNumber())) {
                log.debug(user.getId(), "The user has ID-Koll activated. Use the belonging national id as candidate.");
                return Optional.of(user.getProfile().getFraudPersonNumber());
            }

            Set<String> candidates = credentialsRepository
                    .findAllByUserIdAndType(user.getId(), CredentialsTypes.MOBILE_BANKID).stream()
                    .filter(c -> Objects.equals(c.getStatus(), CredentialsStatus.UPDATED))
                    .map(c -> c.getField(Field.Key.USERNAME))
                    .filter(n -> !Strings.isNullOrEmpty(n))
                    .collect(Collectors.toSet());

            if (candidates.size() == 1) {
                log.debug(user.getId(), "The user has one national id candidate.");
                return candidates.stream().findFirst();
            } else if (candidates.size() > 1) {
                log.debug(user.getId(), "The user has several national id candidates. Returning none.");
                return Optional.empty();
            } else {
                log.debug(user.getId(), "The user has no national id candidate.");
                return Optional.empty();
            }
        }
    }
}
