package se.tink.backend.insights.notifications;

import se.tink.backend.insights.core.domain.model.Insight;

public interface NotificationsGateway {
    void queueInsightNotification(Insight insight);
}
