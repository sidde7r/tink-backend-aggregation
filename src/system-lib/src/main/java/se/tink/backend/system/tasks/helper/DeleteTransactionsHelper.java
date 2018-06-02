package se.tink.backend.system.tasks.helper;

import java.util.List;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.concurrency.TransactionInMemoryReadWriteLock;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.core.UserState;
import se.tink.backend.utils.LogUtils;

public class DeleteTransactionsHelper {

    private static final LogUtils log = new LogUtils(DeleteTransactionsHelper.class);

    private final ServiceContext serviceContext;
    private final CuratorFramework coordinationClient;
    private final UserStateRepository userStateRepository;

    public DeleteTransactionsHelper(ServiceContext serviceContext, CuratorFramework coordinationClient,
            UserStateRepository userStateRepository) {

        this.serviceContext = serviceContext;
        this.coordinationClient = coordinationClient;
        this.userStateRepository = userStateRepository;
    }

    public void delete(String userId, List<String> transactionIds) {
        if (transactionIds == null || transactionIds.size() == 0) {
            log.warn(userId, "No transactionIds to delete.");
            return;
        }

        InterProcessMutex modifyTransactionsLock = new TransactionInMemoryReadWriteLock(coordinationClient,
                userId).getLockForModifyingTransactionsInDatabase();

        try {
            try {
                modifyTransactionsLock.acquire();
            } catch (Exception e) {
                log.error(userId, "Could not take lock. Continuing anyway.");
                // Continuing anyway
            }
            for (String transactionId : transactionIds) {
                try {
                    serviceContext.getSystemServiceFactory().getUpdateService().deleteTransaction(userId, transactionId);
                } catch (Exception e) {
                    log.error(String.format("Could not delete transaction with id=%s.", transactionId), e);
                }
            }

            // The three calls below essentially makes us force a (postponed/lazy) recomputation of statistics
            // and activities next time they are accessed, or a new transaction comes in. We are _not_ doing
            // an immediate recomputation because it would be pretty expensive.

            try {
                serviceContext.getSystemServiceFactory().getUpdateService().deleteAllActivitiesFor(userId);
            } catch (Exception e) {
                log.error(String.format("Could not delete activities for user %s.", userId), e);
            }
            try {
                serviceContext.getSystemServiceFactory().getUpdateService().deleteStatistics(userId);
            } catch (Exception e) {
                log.error(String.format("Could not delete statistics for user %s.", userId), e);
            }
            try {
                UserState userState = userStateRepository.findOneByUserId(userId);
                userState.setStatisticsTimestamp(0);
                userStateRepository.save(userState);
            } catch (Exception e) {
                log.error(String.format("Could not reset statisticsTimestamp for user %s.", userId), e);
            }
        } finally {
            if (modifyTransactionsLock.isAcquiredInThisProcess()) {
                try {
                    modifyTransactionsLock.release();
                } catch (Exception e) {
                    log.error(userId, "Unable to release lock.");
                }
            }
        }
    }
}
