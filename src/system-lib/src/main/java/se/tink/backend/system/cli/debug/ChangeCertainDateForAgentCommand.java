package se.tink.backend.system.cli.debug;

import com.google.common.base.Objects;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.sourceforge.argparse4j.inf.Namespace;
import org.joda.time.DateTime;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.Provider;
import se.tink.backend.core.User;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.cli.helper.traversal.CommandLineInterfaceUserTraverser;
import se.tink.backend.utils.LogUtils;

public class ChangeCertainDateForAgentCommand extends ServiceContextCommand<ServiceConfiguration> {

    private static final LogUtils log = new LogUtils(ChangeCertainDateForAgentCommand.class);
    private static final String DRY_RUN = "dryRun";
    private static final String AGENT_CLASS_NAME = "agentClass";
    private static final String NUMBER_OF_DAYS_TO_REMOVE = "numberOfDaysToRemove";

    private CredentialsRepository credentialsRepository;
    private AccountRepository accountRepository;
    private Map<String, Provider> providerNameMap;

    public ChangeCertainDateForAgentCommand() {
        super("change-agent-certain-date",
                "Change certain date of credential's accounts belonging to a certain agent.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        final UserRepository userRepository = injector.getInstance(UserRepository.class);
        final ProviderRepository providerRepository = injector.getInstance(ProviderRepository.class);
        credentialsRepository = injector.getInstance(CredentialsRepository.class);
        accountRepository = injector.getInstance(AccountRepository.class);

        final List<Provider> providerList = providerRepository.findAll();
        providerNameMap = providerList.stream().collect(Collectors.toMap(Provider::getName, Function.identity()));

        final AtomicInteger accountsCounter = new AtomicInteger();
        final String providerClassName = System.getProperty(AGENT_CLASS_NAME);
        final boolean dryRun = Boolean.getBoolean(DRY_RUN);
        final int numberOfDaysToRemove = Integer.getInteger(NUMBER_OF_DAYS_TO_REMOVE, 30);

        userRepository.streamAll()
                .compose(new CommandLineInterfaceUserTraverser(4))
                .forEach(u -> updateUsersCertainDates(dryRun, u, accountsCounter, providerClassName,
                        numberOfDaysToRemove));

        log.info(String.format("Finished command. Dry run: [%s] - Changed accounts: [%s]", String.valueOf(dryRun),
                accountsCounter.get()));
    }

    private String getProviderClassName(String providerName) {
        Provider provider = providerNameMap.getOrDefault(providerName, null);
        if (provider == null) {
            return null;
        }
        return provider.getClassName();
    }

    private void updateUsersCertainDates(boolean dryRun, User user, AtomicInteger accountsCounter,
            String providerClassName, int numberOfDaysToRemove) {
        credentialsRepository.findAllByUserId(user.getId())
                .stream()
                .filter(c -> providerClassName.equals(getProviderClassName(c.getProviderName())))
                .map(c -> accountRepository.findByCredentialsId(c.getId()))
                .flatMap(List::stream)
                .filter(account -> account.getCertainDate() != null)
                .forEach(account -> {
                    accountsCounter.incrementAndGet();
                    if (dryRun) {
                        return;
                    }

                    updateCertainDate(account, numberOfDaysToRemove);
                    accountRepository.save(account);
                });
    }

    private void updateCertainDate(Account account, int numberOfDaysToRemove) {
        DateTime oldCertainDate = new DateTime(account.getCertainDate());
        DateTime newCertainDate = getNewCertainDate(oldCertainDate, numberOfDaysToRemove);

        if (Objects.equal(oldCertainDate, newCertainDate)) {
            return;
        }

        account.setCertainDate(newCertainDate.toDate());
    }

    private static DateTime getNewCertainDate(DateTime oldCertainDate, Integer numberOfDaysToRemoveFromCertainDate) {
        return oldCertainDate.minusDays(numberOfDaysToRemoveFromCertainDate);
    }
}
