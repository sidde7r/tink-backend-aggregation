package se.tink.backend.common.dao;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import java.util.List;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.common.cache.CacheScope;
import se.tink.libraries.metrics.MeterFactory;
import se.tink.backend.common.repository.mysql.main.ActivityRepository;
import se.tink.backend.common.repository.mysql.main.ActivityRepositoryCustom;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.core.Activity;
import se.tink.backend.core.ActivityContainer;

public class ActivityDao implements ActivityRepositoryCustom {
    private static final int CACHE_EXPIRY = 60 * 60 * 24 * 7; // 7 days

    private final ActivityRepository activityRepository;
    private final CacheClient cacheClient;

    @Inject
    public ActivityDao(ActivityRepository activityRepository, CacheClient cacheClient, MeterFactory meterFactory) {
        this.activityRepository = activityRepository;
        this.cacheClient = cacheClient;
    }

    @Override
    public List<Activity> findByUserId(String userId) {
        List<Activity> activities = new ActivityContainer((byte[]) cacheClient.get(CacheScope.ACTIVITIES_BY_USERID,
                userId))
                .getActivities();

        if (activities == null) {
            // Cache miss.
            activities = activityRepository.findByUserId(userId);

            if (activities != null && !activities.isEmpty()) {
                ActivityContainer container = new ActivityContainer(userId, activities);

                cache(container);
            }
        }

        return activities;
    }

    public Activity findShared(String userId, String activityId) {
        byte[] serializedActivity = (byte[]) cacheClient
                .get(CacheScope.ACTIVITY_BY_USERID_AND_ACTIVITYID, getSharedCacheKey(userId, activityId));
        if (serializedActivity == null) {
            return null;
        }
        return SerializationUtils.deserializeFromBinary(serializedActivity, Activity.class);
    }

    @Override
    public void deleteByUserId(String userId) {
        activityRepository.deleteByUserId(userId);
        cacheClient.delete(CacheScope.ACTIVITIES_BY_USERID, userId);
    }

    @Override
    public void insertOrUpdate(ActivityContainer activityContainer) {
        activityRepository.insertOrUpdate(activityContainer);
    }

    public void cache(ActivityContainer container) {
        cacheClient.set(CacheScope.ACTIVITIES_BY_USERID, container.getUserId(), CACHE_EXPIRY,
                container.getData());
    }

    public void saveShared(String userId, Activity activity) {
        cacheClient.set(CacheScope.ACTIVITY_BY_USERID_AND_ACTIVITYID, getSharedCacheKey(userId, activity.getId()),
                CACHE_EXPIRY, SerializationUtils.serializeToBinary(activity));
    }

    @VisibleForTesting
    String getSharedCacheKey(String userId, String activityId) {
        return userId + activityId;
    }
}
