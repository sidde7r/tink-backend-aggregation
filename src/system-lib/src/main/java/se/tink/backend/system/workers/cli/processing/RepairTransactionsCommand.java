package se.tink.backend.system.workers.cli.processing;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.date.DateUtils;

public class RepairTransactionsCommand extends ServiceContextCommand<ServiceConfiguration> {
    public RepairTransactionsCommand() {
        super("repair-transactions", "Repairs all transactions");
    }

    private static final LogUtils log = new LogUtils(RepairTransactionsCommand.class);

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        UserRepository userRepository = serviceContext.getRepository(UserRepository.class);
        TransactionDao transactionRepository = serviceContext.getDao(TransactionDao.class);
        CredentialsRepository credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        TransactionDao transactionDao = serviceContext.getDao(TransactionDao.class);

        Set<String> providersToProcess = Sets.newHashSet(Lists.newArrayList("eurocard", "seb", "nordea",
                "saseurobonusmastercard"));

        Map<String, String> credentialsIdToProviderMap = new HashMap<String, String>();

        List<Credentials> credentials = credentialsRepository.findAll();

        for (Credentials c : credentials) {
            credentialsIdToProviderMap.put(c.getId(), c.getProviderName());

            if (providersToProcess.contains(c.getProviderName())) {
                c.setUpdated(null);
                c.setStatus(CredentialsStatus.CREATED);

                credentialsRepository.save(c);
            }
        }

        int count = 0;

        for (User user : userRepository.findAll()) {
            log.info(user.getId(), "Doing user");

            List<Transaction> transactionsToSave = Lists.newArrayList();

            for (Transaction transaction : transactionRepository.findAllByUserId(user.getId())) {
                count++;

                String providerName = credentialsIdToProviderMap.get(transaction.getCredentialsId());

                if (providersToProcess.contains(providerName)) {
                    transaction.setDate(DateUtils.flattenTime(transaction.getDate()));
                    transactionsToSave.add(transaction);
                }

                if (count % 10000 == 0) {
                    log.info("Have done " + count);
                }
            }

            transactionDao.saveAndIndex(user, transactionsToSave, false);
        }

        log.info("Done doing " + count + " transactions");
    }
}
