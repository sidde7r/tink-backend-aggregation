package se.tink.backend.common.repository.mysql.main;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.common.cache.CacheScope;
import se.tink.backend.core.UserState;

public class UserStateRepositoryImpl implements UserStateRepositoryCustom {
    private static final int DEFAULT_EXPIRY = 15 * 60;

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void deleteByUserId(String userId) {
        em.createQuery("DELETE from UserState u where u.userId = :userId").setParameter("userId", userId)
                .executeUpdate();
    }

    @Override
    public Long findActivitiesTimestampByUserId(String userId, CacheClient cacheClient) {
        Long timestamp = (Long) cacheClient
                .get(CacheScope.ACTIVITIES_TIMESTAMP_BY_USERID, userId);

        if (timestamp != null) {
            return timestamp;
        }

        UserState userState = findOneByUserId(userId);

        if (userState == null) {
            return null;
        }

        cacheClient.set(CacheScope.ACTIVITIES_TIMESTAMP_BY_USERID, userId, DEFAULT_EXPIRY,
                userState.getActivitiesTimestamp());

        return userState.getActivitiesTimestamp();
    }

    @Override
    public Long findContextTimestampByUserId(String userId, CacheClient cacheClient) {
        Long timestamp = (Long) cacheClient.get(CacheScope.CONTEXT_TIMESTAMP_BY_USERID, userId);

        if (timestamp != null) {
            return timestamp;
        }

        UserState userState = findOneByUserId(userId);

        if (userState == null) {
            return null;
        }

        cacheClient
                .set(CacheScope.CONTEXT_TIMESTAMP_BY_USERID, userId, DEFAULT_EXPIRY, userState.getContextTimestamp());

        return userState.getContextTimestamp();
    }

    @Override
    public UserState findOneByUserId(String userId) {
        try {
            return em.createQuery("select us from UserState us where us.userId = :userId", UserState.class)
                    .setParameter("userId", userId).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Long findStatisticsTimestampByUserId(String userId, CacheClient cacheClient) {
        Long timestamp = (Long) cacheClient.get(CacheScope.STATISTICS_TIMESTAMP_BY_USERID, userId);

        if (timestamp != null) {
            return timestamp;
        }

        UserState userState = findOneByUserId(userId);

        if (userState == null) {
            return null;
        }

        cacheClient.set(CacheScope.STATISTICS_TIMESTAMP_BY_USERID, userId, DEFAULT_EXPIRY,
                userState.getStatisticsTimestamp());

        return userState.getStatisticsTimestamp();
    }

    @Override
    @Transactional
    public void updateActivitiesTimestampByUserId(String userId, Long activitiesTimestamp,
            CacheClient cacheClient) {
        cacheClient.set(CacheScope.ACTIVITIES_TIMESTAMP_BY_USERID, userId, DEFAULT_EXPIRY, activitiesTimestamp);

        em.createQuery("update UserState us set us.activitiesTimestamp=:activitiesTimestamp where us.userId=:userId")
                .setParameter("activitiesTimestamp", activitiesTimestamp).setParameter("userId", userId)
                .executeUpdate();
    }

    @Transactional
    private void updateContextTimestampByUserId(String userId, Long contextTimestamp, CacheClient cacheClient) {
        cacheClient.set(CacheScope.CONTEXT_TIMESTAMP_BY_USERID, userId, DEFAULT_EXPIRY, contextTimestamp);

        em.createQuery("update UserState us set us.contextTimestamp=:contextTimestamp where us.userId=:userId")
                .setParameter("contextTimestamp", contextTimestamp).setParameter("userId", userId).executeUpdate();
    }

    @Transactional
    @Override
    public void updateContextTimestampByUserId(String userId, CacheClient cacheClient) {
        updateContextTimestampByUserId(userId, System.currentTimeMillis(), cacheClient);
    }

    @Transactional
    @Override
    public void updateStatisticsTimestampByUserId(String userId, Long statisticsTimestamp,
            CacheClient cacheClient) {
        cacheClient.set(CacheScope.STATISTICS_TIMESTAMP_BY_USERID, userId, DEFAULT_EXPIRY, statisticsTimestamp);

        em.createQuery("update UserState us set us.statisticsTimestamp=:statisticsTimestamp where us.userId=:userId")
                .setParameter("statisticsTimestamp", statisticsTimestamp).setParameter("userId", userId)
                .executeUpdate();
    }
}
