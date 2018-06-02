package se.tink.backend.main.resources;

import com.google.common.collect.FluentIterable;
import javax.ws.rs.Path;
import se.tink.backend.api.NotificationService;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.dao.NotificationDao;
import se.tink.backend.core.Notification;
import se.tink.backend.core.NotificationStatus;
import se.tink.backend.rpc.NotificationQuery;
import se.tink.backend.rpc.NotificationQueryResponse;

@Path("/api/v1/notifications")
public class NotificationServiceResource implements NotificationService {
    private final NotificationDao notificationDao;

    public NotificationServiceResource(ServiceContext context) {
        this.notificationDao = context.getDao(NotificationDao.class);
    }

    @Override
    public void received(String id) {
        notificationDao.setStatusById(id, NotificationStatus.RECEIVED);
    }

    @Override
    public void read(String id) {
        notificationDao.setStatusById(id, NotificationStatus.READ);
    }

    @Override
    public NotificationQueryResponse queryNotifications(AuthenticatedUser authenticatedUser, final NotificationQuery query) {
        FluentIterable<Notification> notifications = FluentIterable
                .from(notificationDao.findByUserId(authenticatedUser.getUser().getId()))
                .filter(notification -> {
                    if (query.getStatuses() != null && !query.getStatuses().isEmpty() && !query.getStatuses()
                            .contains(notification.getStatus())) {
                        return false;
                    }

                    return true;
                });

        NotificationQueryResponse response = new NotificationQueryResponse();

        response.setCount(notifications.size());

        if (query.getLimit() != 0) {
            response.setNotifications(notifications.skip(query.getOffset()).limit(query.getLimit()).toList());
        } else {
            response.setNotifications(notifications.skip(query.getOffset()).toList());
        }

        return response;
    }
}
