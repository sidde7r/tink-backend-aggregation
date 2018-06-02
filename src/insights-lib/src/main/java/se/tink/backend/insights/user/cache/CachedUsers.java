package se.tink.backend.insights.user.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import se.tink.backend.core.User;
import se.tink.backend.insights.core.valueobjects.UserId;

public class CachedUsers {

    private static final long EXPIRATION_TIME = TimeUnit.SECONDS.toMillis(5);
    private static final long CLEAN_UP_TIME = TimeUnit.SECONDS.toMillis(10);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final LoadingCache<String, User> expiringCache;

    @Inject
    public CachedUsers() {
        this.expiringCache =  CacheBuilder.newBuilder()
                .expireAfterAccess(EXPIRATION_TIME, TimeUnit.MILLISECONDS)
                .build(new CacheLoader<String, User>() {
                    @Override
                    public User load(String userId) {
                        return null;
                    }
                });

        scheduler.schedule(expiringCache::cleanUp, CLEAN_UP_TIME, TimeUnit.MILLISECONDS);
    }


    public void save(UserId userId, User user) {
        expiringCache.put(userId.value(), user);
    }

    public User getIfAny(UserId userId) {
        return expiringCache.getIfPresent(userId.value());
    }


}
