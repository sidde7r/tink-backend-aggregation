package se.tink.backend.api;

import java.util.List;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.core.insights.InsightsResponse;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.StatisticQuery;

public interface StatisticsService {

    List<Statistic> list(AuthenticatedUser user);

    List<Statistic> query(AuthenticatedUser authenticatedUser, StatisticQuery query);

    List<Statistic> queries(AuthenticatedUser authenticatedUser, List<StatisticQuery> queries);

    InsightsResponse insights(AuthenticatedUser authenticatedUser);
}
