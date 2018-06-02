package se.tink.backend.insights.queryservice;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.insights.app.queryservices.InsightQueryService;
import se.tink.backend.insights.app.repositories.InsightRepository;
import se.tink.backend.insights.core.domain.model.Insight;
import se.tink.backend.insights.core.valueobjects.UserId;
import se.tink.backend.insights.queryservice.relevance.RelevanceEngine;

public class InsightQueryServiceImpl implements InsightQueryService {
    private final InsightRepository insightRepository;
    private final UserRepository userRepository;
    private final RelevanceEngine relevanceEngine;

    @Inject
    public InsightQueryServiceImpl(InsightRepository insightRepository,
            UserRepository userRepository, RelevanceEngine relevanceEngine) {
        this.insightRepository = insightRepository;
        this.userRepository = userRepository;
        this.relevanceEngine = relevanceEngine;
    }

    @Override
    public List<Insight> fetchInsights(UserId userId) {
        List<Insight> insights = insightRepository.findAllByUserId(userId);
        return relevanceEngine.order(insights);
    }

    @Override
    public List<Insight> fetchInsightsFromOffsetWithLimit(UserId userId, int offset, int limit) {
        List<Insight> insights = fetchInsights(userId).stream().filter(i -> i.validPeriod(new Date()))
                .collect(Collectors.toList());
        final int maxIndex = Math.min(insights.size(), offset + limit);
        if (offset < insights.size()) {
            return insights.subList(offset, maxIndex);  // Sublist high endpoint is exclusive
        }

        return Lists.newArrayList();
    }

    @Override
    public boolean userExists(UserId userId) {
        return userRepository.findOne(userId.value()) != null;
    }
}
