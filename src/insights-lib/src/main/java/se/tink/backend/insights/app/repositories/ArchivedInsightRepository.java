package se.tink.backend.insights.app.repositories;

import java.util.List;
import se.tink.backend.insights.core.domain.model.Insight;
import se.tink.backend.insights.core.valueobjects.UserId;
import se.tink.backend.insights.core.domain.model.ArchivedInsight;
import se.tink.backend.insights.core.valueobjects.InsightId;

public interface ArchivedInsightRepository {
    ArchivedInsight save(Insight insight);
    List<ArchivedInsight> findAllByUserId(UserId userid);
}
