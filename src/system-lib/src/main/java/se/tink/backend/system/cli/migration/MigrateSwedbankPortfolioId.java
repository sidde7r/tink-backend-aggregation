package se.tink.backend.system.cli.migration;

import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.cassandra.PortfolioRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.Portfolio;
import se.tink.backend.core.User;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.cli.helper.traversal.CommandLineInterfaceUserTraverser;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.uuid.UUIDUtils;

// The purpose of this command:
//  - There are duplicate ISK portfolios on Swedbank credentials due to a changing uniqueIdentifier.
// The objective of this command:
//  - Remove all duplicate portofolios PER investment account
//  - Set a new uniqueIdentifier on the remaining portfolio (taken from account unique id)
public class MigrateSwedbankPortfolioId extends ServiceContextCommand<ServiceConfiguration> {

    private static final LogUtils log = new LogUtils(MigrateSwedbankPortfolioId.class);

    public MigrateSwedbankPortfolioId() {
        super("migrate-swedbank-portfolio-uniqueid",
                "Migrate Swedbank's ISK portfolios uniqueId to use account number.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        log.info("Migrate Swedbank portoflio ids.");

        final UserRepository userRepository = injector.getInstance(UserRepository.class);
        final CredentialsRepository credentialsRepository = injector.getInstance(CredentialsRepository.class);
        final AccountRepository accountRepository = injector.getInstance(AccountRepository.class);
        final PortfolioRepository portfolioRepository = injector.getInstance(PortfolioRepository.class);

        userRepository.streamAll()
                .compose(new CommandLineInterfaceUserTraverser(4))
                .forEach(user -> migrateUsersPortfolios(credentialsRepository, accountRepository, portfolioRepository,
                        user));
    }

    private void migrateUsersPortfolios(CredentialsRepository credentialsRepository,
            AccountRepository accountRepository, PortfolioRepository portfolioRepository, User user) {

        credentialsRepository.findAllByUserId(user.getId())
                .stream()
                .filter(c -> c.getProviderName().startsWith("savingsbank-") ||
                        c.getProviderName().startsWith("swedbank-")
                )
                .map(c -> accountRepository.findByCredentialsId(c.getId()))
                .flatMap(List::stream)
                .filter(account -> account.getType() == AccountTypes.INVESTMENT)
                .forEach(account -> {

                    List<Portfolio> portfolios = portfolioRepository.findAllByUserIdAndAccountId(
                            UUIDUtils.fromTinkUUID(account.getUserId()), UUIDUtils.fromTinkUUID(account.getId()))
                            .stream()
                            .filter(p -> p.getType() == Portfolio.Type.ISK)
                            .collect(Collectors.toList());

                    if (portfolios.isEmpty()) {
                        return;
                    }

                    // Delete all but last portfolio which will receive a new unique identifier
                    for (Iterator<Portfolio> it = portfolios.iterator(); it.hasNext(); ) {
                        Portfolio portfolio = it.next();
                        if (it.hasNext()) {
                            portfolioRepository.delete(portfolio);
                        } else {
                            portfolio.setUniqueIdentifier(account.getBankId());
                            portfolioRepository.save(portfolio);
                        }
                    }
                });
    }
}
