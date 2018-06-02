package se.tink.backend.insights.queryservice;

import se.tink.backend.insights.core.domain.model.Insight;
import se.tink.backend.insights.core.valueobjects.UserId;

import java.util.List;

public interface InsightQueryService {

    // Returns a list of ordered insights for the userId
    List<Insight> fetchInsights(UserId userId);
}
