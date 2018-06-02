package se.tink.backend.system.controllers;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import java.util.concurrent.atomic.AtomicInteger;
import se.tink.backend.common.controllers.DeleteController;
import se.tink.backend.common.repository.mysql.main.DeletedUserRepository;
import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.core.DeletedUser;
import se.tink.backend.core.DeletedUserStatus;
import se.tink.backend.rpc.DeleteUserRequest;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

public class DeletedUserController {
    private static final LogUtils log = new LogUtils(DeletedUserController.class);

    private static final ImmutableSet<DeletedUserStatus> PARTIALLY_DELETED_STATUSES = ImmutableSet
            .of(DeletedUserStatus.IN_PROGRESS, DeletedUserStatus.FAILED);

    private final DeletedUserRepository deletedUserRepository;
    private final DeleteController deleteController;
    private final Counter partiallyDeletedUsersCouter;

    @Inject
    public DeletedUserController(DeletedUserRepository deletedUserRepository, DeleteController deleteController,
            MetricRegistry metricRegistry) {
        this.deletedUserRepository = deletedUserRepository;
        this.deleteController = deleteController;
        this.partiallyDeletedUsersCouter = metricRegistry.meter(MetricId.newId("partially_deleted_users"));
    }

    /**
     * Delete those users that have been partially deleted.
     */
    public void deletePartiallyDeletedUsers() {
        log.info("Starting deleting partially deleted users.");

        final AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger failedCount = new AtomicInteger(0);

        deletedUserRepository
                .findAllByStatusIn(PARTIALLY_DELETED_STATUSES)
                .stream()
                .filter(DeletedUser::isPartiallyDeleted)
                .forEach((deleteUser) ->
                {
                    try {
                        log.info(deleteUser.getUserId(), "Starting deletion of user.");
                        DeleteUserRequest request = new DeleteUserRequest();
                        request.setUserId(deleteUser.getUserId());
                        deleteController.deleteUserSynchronous(request);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        failedCount.incrementAndGet();
                        log.error(deleteUser.getUserId(), "Could not delete user", e);
                    }
                });

        partiallyDeletedUsersCouter.inc(failedCount.get());

        log.info(String.format("Finished deleting partially deleted users (Succeeded = '%d', Failed = '%d')",
                successCount.get(), failedCount.get()));
    }
}
