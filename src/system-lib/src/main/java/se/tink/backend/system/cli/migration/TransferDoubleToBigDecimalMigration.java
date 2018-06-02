package se.tink.backend.system.cli.migration;

import com.google.common.collect.Lists;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.cassandra.TransferRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.cli.helper.traversal.CommandLineInterfaceUserTraverser;
import se.tink.backend.utils.LogUtils;

public class TransferDoubleToBigDecimalMigration extends ServiceContextCommand<ServiceConfiguration> {
    private static final LogUtils log = new LogUtils(TransferDoubleToBigDecimalMigration.class);
    private static final int SAVE_BATCH_SIZE = 50;
    private TransferRepository transferRepository;

    public TransferDoubleToBigDecimalMigration() {
        super("migrate-transfer-double-value", "Migrate `amount` Double to `exactAmount` BigDecimal");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        transferRepository = serviceContext.getRepository(TransferRepository.class);
        final UserRepository userRepository = serviceContext.getRepository(UserRepository.class);

        userRepository.streamAll()
                .compose(new CommandLineInterfaceUserTraverser(20))
                .forEach(user -> {
                    try {
                        doMigration(user.getId());
                    } catch (Exception e) {
                        log.error("Failed to migrate transfers for userId: " + user.getId(), e);
                    }
                });
    }

    private void doMigration(String userId) {
        List<Transfer> userTransfers = transferRepository.findAllByUserId(userId);

        for (Transfer transfer : userTransfers) {
            // When we set amount, we set this amount to both columns: amount and exactAmount
            transfer.setAmount(transfer.getAmount());
        }

        // save transfers in batch
        for (List<Transfer> transfers : Lists.partition(userTransfers, SAVE_BATCH_SIZE)) {
            transferRepository.save(transfers);
        }
    }
}
