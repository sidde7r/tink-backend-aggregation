package se.tink.backend.system.cli.migration;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.transactions.TransactionRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Transaction;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.cli.helper.traversal.CommandLineInterfaceUserTraverser;

public class CleanDuplicateTransactionIdsCommand extends ServiceContextCommand<ServiceConfiguration> {
    private static final Logger log = LoggerFactory.getLogger(CleanDuplicateTransactionIdsCommand.class);
    private TransactionRepository transactionByUserIdAndPeriodRepository;
    public CleanDuplicateTransactionIdsCommand() {
        super("clean-duplicate-transactions",
                "Restore transaction for affected user");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
                       ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        transactionByUserIdAndPeriodRepository = serviceContext.getRepository(TransactionRepository.class);
        UserRepository userRepository = serviceContext.getRepository(UserRepository.class);
        DateTime incidentDate = new DateTime(2017, 11, 20,0,0);
        DateTime cutOff = new DateTime(2017, 12, 31,0,0);

        try {
            userRepository.streamAll()
                    .compose(new CommandLineInterfaceUserTraverser(20))
                    .map(u -> transactionByUserIdAndPeriodRepository
                            .findByUserIdAndTime(u.getId(), incidentDate, cutOff))
                    .forEach(this::removeDuplicatePending);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void removeDuplicatePending(List<Transaction> transactions) {
        List<Transaction> pending = getTransactionsToDelete(transactions);
        deletePending(pending);
    }

    public List<Transaction> getTransactionsToDelete(List<Transaction> transactions) {
        Map<String, List<Transaction>> groupedTransactions = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getId));

        List<Map.Entry<String, List<Transaction>>> filteredTransactions = groupedTransactions.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1).collect(Collectors.toList());
        log.info("{} duplicates found", filteredTransactions.size());

        List<Transaction> pendingTransactions = filteredTransactions.stream()
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .filter(Transaction::isPending)
                .collect(Collectors.toList());
        log.info("found {} transactions to delete ", pendingTransactions.size());
        return pendingTransactions;
    }

    private void deletePending(List<Transaction> pending) {
        if (!pending.isEmpty()) {
            transactionByUserIdAndPeriodRepository.delete(pending);
            log.info("{} pending transactions removed for user {}", pending.size(), pending.get(0).getUserId());
        }
    }
}
