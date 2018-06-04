package se.tink.backend.common.dao;

import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.common.cache.CacheScope;
import se.tink.libraries.metrics.MeterFactory;
import se.tink.backend.common.repository.mysql.main.ActivityRepository;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.core.Activity;
import se.tink.backend.core.ActivityContainer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Answers.RETURNS_MOCKS;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ActivityDaoTest {
    @InjectMocks private ActivityDao activityDao;
    @Mock private ActivityRepository activityRepository;
    @Mock private CacheClient cacheClient;
    @Mock(answer = RETURNS_MOCKS) private MeterFactory meterFactory;

    @Test
    public void verifyDeletingDataFromDbAndCache() {
        String userId = "userId";
        activityDao.deleteByUserId(userId);
        verify(cacheClient).delete(eq(CacheScope.ACTIVITIES_BY_USERID), eq(userId));
        verify(activityRepository).deleteByUserId(eq(userId));
    }

    @Test
    public void verifyCacheData() {
        String userId = "userId";
        ActivityContainer container = new ActivityContainer(userId, Collections.singletonList(new Activity()));
        byte[] data = container.getData();

        activityDao.cache(container);
        verify(cacheClient).set(eq(CacheScope.ACTIVITIES_BY_USERID), eq(userId), anyInt(), eq(data));
    }

    @Test
    public void verifySaveDataToDb() {
        String userId = "userId";
        ActivityContainer container = new ActivityContainer();
        container.setUserId(userId);
        activityDao.insertOrUpdate(container);
        verify(activityRepository).insertOrUpdate(eq(container));
    }

    @Test
    public void getActivityFromCache() {
        String userId = "userId";
        when(cacheClient.get(eq(CacheScope.ACTIVITIES_BY_USERID), eq(userId))).thenReturn(new ActivityContainer(userId,
                Collections.singletonList(createActivityWithMessage("Cache"))).getData());
        List<Activity> activities = activityDao.findByUserId(userId);

        assertEquals(1, activities.size());
        assertEquals("Cache", activities.get(0).getMessage());
    }

    @Test
    public void getStatisticsFromDb() {
        String userId = "userId";
        when(activityRepository.findByUserId(eq(userId)))
                .thenReturn(Collections.singletonList(createActivityWithMessage("DB")));
        List<Activity> activities = activityDao.findByUserId(userId);

        assertEquals(1, activities.size());
        assertEquals("DB", activities.get(0).getMessage());
        verify(cacheClient)
                .set(eq(CacheScope.ACTIVITIES_BY_USERID), eq(userId),
                        anyInt(), eq(new ActivityContainer(userId, activities).getData()));
    }

    @Test
    public void verifySaveSharedData() {
        String userId = "userId";
        Activity activity = new Activity();
        String activityId = activity.getId();

        activityDao.saveShared(userId, activity);
        verify(cacheClient).set(eq(CacheScope.ACTIVITY_BY_USERID_AND_ACTIVITYID), eq(activityDao.getSharedCacheKey
                        (userId,
                activityId)),
                anyInt(), eq(SerializationUtils.serializeToBinary(activity)));
    }

    @Test
    public void getSharedData() {
        String userId = "userId";
        Activity activity = new Activity();
        String activityId = activity.getId();

        when(cacheClient.get(eq(CacheScope.ACTIVITY_BY_USERID_AND_ACTIVITYID),
                eq(activityDao.getSharedCacheKey(userId, activityId))))
                .thenReturn(SerializationUtils.serializeToBinary(activity));
        Activity savedActivity = activityDao.findShared(userId, activityId);
        assertNotNull(savedActivity);
        assertEquals(activityId, savedActivity.getId());
    }

    @Test
    public void getNullSharedDataOnNoDataInCache() {
        Activity savedActivity = activityDao.findShared("userId", "activityId");
        assertNull(savedActivity);
    }

    private Activity createActivityWithMessage(String message) {
        Activity activity = new Activity();
        activity.setMessage(message);
        return activity;
    }

}
