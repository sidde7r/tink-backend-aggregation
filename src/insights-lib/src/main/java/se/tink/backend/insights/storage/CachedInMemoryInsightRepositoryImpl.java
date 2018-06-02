package se.tink.backend.insights.storage;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import se.tink.backend.insights.app.repositories.InsightRepository;
import se.tink.backend.insights.core.domain.model.Insight;
import se.tink.backend.insights.core.valueobjects.InsightId;
import se.tink.backend.insights.core.valueobjects.UserId;

public class CachedInMemoryInsightRepositoryImpl implements InsightRepository {

    private static final long EXPIRATION_TIME = TimeUnit.MINUTES.toMillis(50);
    private static final long CLEAN_UP_TIME = TimeUnit.MINUTES.toMillis(60);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final LoadingCache<String, List<Insight>> expiringCache;

    @Inject
    public CachedInMemoryInsightRepositoryImpl() {
        this.expiringCache =  CacheBuilder.newBuilder()
                .expireAfterAccess(EXPIRATION_TIME, TimeUnit.MILLISECONDS)
                .build(new CacheLoader<String, List<Insight>>() {
                    @Override
                    public List<Insight> load(String userId) {
                        return Lists.newArrayList();
                    }
                });

        scheduler.schedule(expiringCache::cleanUp, CLEAN_UP_TIME, TimeUnit.MILLISECONDS);
    }

    @Override
    public void save(UserId userId, Insight insight) {
        List<Insight> insights = expiringCache.getIfPresent(userId.value());
        if (insights == null) {
            insights = Lists.newArrayList();
        }
        insights.add(insight);
        expiringCache.put(userId.value(), insights);
    }

    @Override
    public void save(UserId userId, List<Insight> insightList) {
        List<Insight> insights = expiringCache.getIfPresent(userId);
        if (insights == null) {
            insights = Lists.newArrayList();
        }
        insights.addAll(insightList);
        expiringCache.put(userId.value(), insights);
    }

    @Override
    public void deleteByInsightId(UserId userId, InsightId insightId) {
        List<Insight> insights = expiringCache.getIfPresent(userId.value());
        if (insights == null || insights.size() ==0) {
            return;
        }
        for (Insight insight : insights) {
            if (insight.getId().equals(insightId)) {
                insights.remove(insight);
                return;
            }
        }
    }

    @Override
    public List<Insight> findAllByUserId(UserId userId) {
        List<Insight> insights = expiringCache.getIfPresent(userId.value());
        if (insights == null) {
            insights = Lists.newArrayList();
        }
        return insights;
    }

    @Override
    public Insight findByUserIdAndInsightId(UserId userId, InsightId insightId) {
        List<Insight> insights = expiringCache.getIfPresent(userId.value());
        if (insights == null || insights.size() ==0) {
            return null;
        }

        for (Insight insight : insights) {
            if (insight.getId().equals(insightId)){
                return insight;
            }
        }

        return null;
    }

    @Override
    public void deleteByUserId(UserId userId) {
        expiringCache.invalidate(userId.value());
    }
}
