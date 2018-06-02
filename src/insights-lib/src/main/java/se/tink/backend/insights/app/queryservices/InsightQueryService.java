package se.tink.backend.insights.app.queryservices;

import java.util.List;
import se.tink.backend.insights.core.domain.model.Insight;
import se.tink.backend.insights.core.valueobjects.UserId;

public interface InsightQueryService {

    // Returns a list of ordered insights for the userId
    List<Insight> fetchInsights(UserId userId);

    List<Insight> fetchInsightsFromOffsetWithLimit(UserId userId, int offset, int limit);

    // TODO: add this logic to the user query service
    boolean userExists(UserId userId);
}
