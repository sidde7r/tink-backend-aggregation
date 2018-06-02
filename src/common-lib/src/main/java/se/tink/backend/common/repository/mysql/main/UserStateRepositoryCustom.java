package se.tink.backend.common.repository.mysql.main;

import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.core.UserState;

public interface UserStateRepositoryCustom {
    void deleteByUserId(String userId);

    void updateContextTimestampByUserId(String userId, CacheClient cacheClient);

    void updateStatisticsTimestampByUserId(String userId, Long statisticsTimestamp,
            CacheClient cacheClient);

    void updateActivitiesTimestampByUserId(String userId, Long activitiesTimestamp,
            CacheClient cacheClient);

    Long findContextTimestampByUserId(String userId, CacheClient cacheClient);

    Long findStatisticsTimestampByUserId(String userId, CacheClient cacheClient);

    Long findActivitiesTimestampByUserId(String userId, CacheClient cacheClient);

    UserState findOneByUserId(String userId);

}
