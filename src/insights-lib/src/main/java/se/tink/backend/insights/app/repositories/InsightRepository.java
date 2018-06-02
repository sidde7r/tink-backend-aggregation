package se.tink.backend.insights.app.repositories;

import java.util.List;
import se.tink.backend.insights.core.domain.model.Insight;
import se.tink.backend.insights.core.valueobjects.InsightId;
import se.tink.backend.insights.core.valueobjects.UserId;

public interface InsightRepository {
    void save(UserId userId, Insight insight);

    void save(UserId userId, List<Insight> insights);

    default void delete(Insight insight) {
        deleteByInsightId(insight.getUserId(), insight.getId());
    }

    void deleteByInsightId(UserId userId, InsightId insightId);

    List<Insight> findAllByUserId(UserId userid);

    Insight findByUserIdAndInsightId(UserId userid, InsightId insightId);

    void deleteByUserId(UserId userId);
}
