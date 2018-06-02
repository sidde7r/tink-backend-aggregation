package se.tink.backend.insights.storage;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import se.tink.backend.insights.app.repositories.ArchivedInsightRepository;
import se.tink.backend.insights.core.domain.model.ArchivedInsight;
import se.tink.backend.insights.core.domain.model.Insight;
import se.tink.backend.insights.core.valueobjects.UserId;

public class CachedInMemoryArchivedInsightRepositoryImpl implements ArchivedInsightRepository {

    private static final long EXPIRATION_TIME = TimeUnit.MINUTES.toMillis(50);
    private static final long CLEAN_UP_TIME = TimeUnit.MINUTES.toMillis(60);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final LoadingCache<String, List<ArchivedInsight>> expiringCache;

    @Inject
    public CachedInMemoryArchivedInsightRepositoryImpl() {
        this.expiringCache =  CacheBuilder.newBuilder()
                .expireAfterAccess(EXPIRATION_TIME, TimeUnit.MILLISECONDS)
                .build(new CacheLoader<String, List<ArchivedInsight>>() {
                    @Override
                    public List<ArchivedInsight> load(String userId) {
                        return com.google.common.collect.Lists.newArrayList();
                    }
                });

        scheduler.schedule(expiringCache::cleanUp, CLEAN_UP_TIME, TimeUnit.MILLISECONDS);
    }

    @Override
    public ArchivedInsight save(Insight insight) {
        ArchivedInsight archivedInsight = new ArchivedInsight(
                insight.getId(),
                insight.getUserId(),
                insight.getType(),
                insight.getSelectedAction(),
                insight.composeMessage(),
                insight.getCreated());

        List<ArchivedInsight> archivedInsights = findAllByUserId(insight.getUserId());
        archivedInsights.add(archivedInsight);
        expiringCache.put(insight.getUserId().value(), archivedInsights);
        return archivedInsight;
    }

    @Override
    public List<ArchivedInsight> findAllByUserId(UserId userId) {
        List<ArchivedInsight> archivedInsights = expiringCache.getIfPresent(userId.value());
        if (archivedInsights == null) {
            archivedInsights = Lists.newArrayList();
        }
        return archivedInsights;
    }

}
