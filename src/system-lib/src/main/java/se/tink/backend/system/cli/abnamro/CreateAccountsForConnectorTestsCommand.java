package se.tink.backend.system.cli.abnamro;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.Date;
import java.util.List;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.libraries.abnamro.utils.AbnAmroLegacyUserUtils;
import se.tink.libraries.abnamro.utils.AbnAmroUtils;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.MarketRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.Market;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;

public class CreateAccountsForConnectorTestsCommand extends ServiceContextCommand<ServiceConfiguration> {

    private static final LogUtils log = new LogUtils(CreateAccountsForConnectorTestsCommand.class);

    private static UserRepository userRepository;
    private static CredentialsRepository credentialsRepository;
    private static AccountRepository accountRepository;

    public CreateAccountsForConnectorTestsCommand() {
        super("abn-create-test-accounts", "Create test users for ABN AMRO tests.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, final ServiceContext serviceContext) throws Exception {

        if (!Objects.equal(configuration.getCluster(), Cluster.ABNAMRO)) {
            log.error("This command is only enabled in the ABN AMRO cluster.");
            return;
        }

        if (!configuration.isDevelopmentMode()) {
            log.error("This command is only enabled in development mode for now.");
            return;
        }

        userRepository = serviceContext.getRepository(UserRepository.class);
        credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        accountRepository = serviceContext.getRepository(AccountRepository.class);

        Integer count = Integer.getInteger("count", null);
        Long start = Long.getLong("start", null);
        String marketInput = System.getProperty("market", null);

        Preconditions.checkArgument(count != null, "`count` must be specified");
        Preconditions.checkArgument(start != null, "`start` must be specified");
        Preconditions.checkArgument(marketInput != null, "`market` must be specified");

        Market market = serviceContext.getRepository(MarketRepository.class).findOne(marketInput);

        Preconditions.checkState(market != null, "`market` not found");

        for (int i = 0; i < count; i++) {

            Long accountNumber = start + i;

            User user = createUser(accountNumber, market);

            Credentials credentials = createCredentials(user.getId(), accountNumber);

            createAccount(credentials, accountNumber);
        }
    }

    private void createAccount(Credentials credentials, long accountNumber) {
        Account account = accountRepository
                .findByUserIdAndCredentialsIdAndBankId(credentials.getUserId(), credentials.getId(),
                        String.valueOf(accountNumber));

        if (account == null) {
            account = new Account();
            account.setUserId(credentials.getUserId());
            account.setCredentialsId(credentials.getId());
            account.setAccountNumber(String.valueOf(accountNumber));
            account.setBankId(String.valueOf(accountNumber));
            account.setType(AccountTypes.CHECKING); // Doesn't matter which type as long as it isn't null
        }

        accountRepository.save(account);
    }

    private Credentials createCredentials(String userId, Long accountNumber) {

        List<Credentials> credentialsList =
                credentialsRepository.findAllByUserIdAndProviderName(userId, AbnAmroUtils.ABN_AMRO_PROVIDER_NAME);

        Credentials credentials = Iterables.getFirst(credentialsList, null);

        if (credentials == null) {
            credentials = new Credentials();
        }

        credentials.setUserId(userId);
        credentials.setProviderName(AbnAmroUtils.ABN_AMRO_PROVIDER_NAME);
        credentials.setStatus(CredentialsStatus.UPDATED);

        // This should be bc number but using account number here for simplicity
        credentials.setField(AbnAmroUtils.BC_NUMBER_FIELD_NAME, accountNumber.toString());

        credentialsRepository.save(credentials);

        return credentials;
    }

    private User createUser(long accountNumber, Market market) {
        User oldUser = userRepository.findOneByUsername(generateUserName(accountNumber));

        User user = (oldUser == null ? new User() : oldUser);

        UserProfile profile = UserProfile.createDefault(market);

        user.setProfile(profile);
        user.setUsername(generateUserName(accountNumber));
        user.setFlags(Lists.newArrayList(FeatureFlags.TINK_TEST_ACCOUNT));
        user.setCreated(new Date());

        userRepository.save(user);

        log.info(String.format("Created or updated user %s.", user.getUsername()));

        return user;
    }

    private static String generateUserName(Long accountNumber) {
        // This should be bc number but using account number here for simplicity
        return AbnAmroLegacyUserUtils.getUsername(accountNumber.toString());
    }
}
