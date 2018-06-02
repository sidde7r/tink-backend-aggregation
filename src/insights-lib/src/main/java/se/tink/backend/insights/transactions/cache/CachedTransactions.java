package se.tink.backend.insights.transactions.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import se.tink.backend.insights.core.domain.model.InsightTransaction;
import se.tink.backend.insights.core.valueobjects.UserId;

public class CachedTransactions {

    private static final long EXPIRATION_TIME = TimeUnit.SECONDS.toMillis(5);
    private static final long CLEAN_UP_TIME = TimeUnit.SECONDS.toMillis(10);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final LoadingCache<String, List<InsightTransaction>> expiringCache;

    @Inject
    public CachedTransactions() {
        this.expiringCache =  CacheBuilder.newBuilder()
                .expireAfterAccess(EXPIRATION_TIME, TimeUnit.MILLISECONDS)
                .build(new CacheLoader<String, List<InsightTransaction>>() {
                    @Override
                    public List<InsightTransaction> load(String userId) {
                        return Lists.newArrayList();
                    }
                });

        scheduler.schedule(expiringCache::cleanUp, CLEAN_UP_TIME, TimeUnit.MILLISECONDS);
    }


    public void save(UserId userId, List<InsightTransaction> transactions) {
        expiringCache.put(userId.value(), transactions);
    }

    public List<InsightTransaction> getIfAny(UserId userId) {
        return expiringCache.getIfPresent(userId.value());
    }


}
