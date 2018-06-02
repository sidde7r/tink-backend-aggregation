package se.tink.backend.main.controllers;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.assertj.core.util.Strings;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import se.tink.backend.common.concurrency.LockFactory;
import se.tink.backend.common.concurrency.StatisticsActivitiesLock;
import se.tink.backend.common.dao.ActivityDao;
import se.tink.backend.common.exceptions.LockException;
import se.tink.libraries.metrics.MeterFactory;
import se.tink.backend.common.repository.mysql.main.FeedbackRepository;
import se.tink.backend.core.Activity;
import se.tink.backend.rpc.ActivityQuery;
import se.tink.backend.rpc.ActivityQueryResponse;
import se.tink.backend.rpc.Feedback;
import se.tink.libraries.date.DateUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Answers.RETURNS_MOCKS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(JUnitParamsRunner.class)
public class ActivityServiceControllerTest {
    @Rule public MockitoRule rule = MockitoJUnit.rule();
    @InjectMocks private ActivityServiceController activityServiceController;
    @Mock private ActivityDao activityDao;
    @Mock private FeedbackRepository feedbackRepository;
    @Mock private LockFactory lockFactory;
    @Mock private StatisticsActivitiesLock statisticsActivitiesLock;
    @Mock(answer = RETURNS_MOCKS) private MeterFactory meterFactory;

    @Before
    public void setUp() throws LockException {
        when(activityDao.findByUserId("userId")).thenReturn(generateActivitiesList());
        when(lockFactory.getStatisticsAndActivitiesLock(anyString())).thenReturn(statisticsActivitiesLock);
        when(statisticsActivitiesLock.waitForRead(anyLong(), any(TimeUnit.class))).thenReturn(true);
    }

    private List<Activity> generateActivitiesList() {
        return Arrays.asList(createActivity("activityId1", "key1", Activity.Types.MONTHLY_SUMMARY,
                DateUtils.parseDate("2016-11-01")),
                createActivity("activityId2", "key2", Activity.Types.BALANCE_HIGH, DateUtils.parseDate("2016-10-01")),
                createActivity("activityId4", "key4", Activity.Types.BALANCE_HIGH, DateUtils.parseDate("2016-06-06")),
                createActivity("activityId5", "key5", Activity.Types.BADGE, DateUtils.parseDate("2016-08-06")));
    }

    @Test
    public void saveFeedbackOnExistedActivity() {
        activityServiceController.feedback("userId", "activityId1", "My feedback");
        verify(feedbackRepository).save(any(Feedback.class));
    }

    @Test(expected = NoSuchElementException.class)
    public void saveFeedbackOnNonExistedActivity() {
        activityServiceController.feedback("userId", "nonExistedActivityId", "My feedback");
    }

    @Test
    public void getActivityByAgent() throws LockException {
        Activity activity = activityServiceController.get("userId", "Tink Mobile/6.7.4 (iOS; 6.0, iPhone)", "key2");
        assertNotNull(activity);
        assertEquals("activityId2", activity.getId());
    }

    @Test
    public void getActivityWithoutAgent() throws LockException {
        String nullUserAgent = null;
        Activity activity = activityServiceController.get("userId", nullUserAgent, "key2");
        assertNotNull(activity);
        assertEquals("activityId2", activity.getId());
    }

    @Test
    public void getActivityByEmptyAgent() throws LockException {
        String emptyUserAgent = "";
        Activity activity = activityServiceController.get("userId", emptyUserAgent, "key2");
        assertNotNull(activity);
        assertEquals("activityId2", activity.getId());
    }

    @Test(expected = NoSuchElementException.class)
    public void throwExceptionOnInvalidUserAgent() throws LockException {
        activityServiceController.get("userId", "Tink Mobile/1.7.4 (iOS; 8.1, iPhone Simulator)", "key2");
    }

    @Test(expected = NoSuchElementException.class)
    public void throwExceptionOnUnknownKey() throws LockException {
        String noUserAgent = null;
        activityServiceController.get("userId", noUserAgent, "key3");
    }

    @Test(expected = NoSuchElementException.class)
    public void throwExceptionOnUnknownUserId() throws LockException {
        String noUserAgent = "";
        activityServiceController.get("userId2", noUserAgent, "key2");
    }

    @Test(expected = LockException.class)
    public void throwExceptionOnExceptionOnLockWaitingForGettingOneActivity() throws LockException {
        when(statisticsActivitiesLock.waitForRead(anyLong(), any(TimeUnit.class))).thenThrow(new LockException());
        String noUserAgent = "";
        activityServiceController.get("userId", noUserAgent, "key1");
    }

    @Test
    public void returnActivityOnFalseLockWaiting() throws LockException {
        when(statisticsActivitiesLock.waitForRead(anyLong(), any(TimeUnit.class))).thenReturn(false);
        String noUserAgent = "";
        Activity activity = activityServiceController.get("userId", noUserAgent, "key1");
        assertNotNull(activity);
        assertEquals("activityId1", activity.getId());
    }

    @Test
    @Parameters({
            "application, 0",
            "monthly-summary, 1",
            "balance-high, 2"
    })
    public void filterActivitiesByType(String type, int activitySize) throws LockException {
        ActivityQuery activityQuery = new ActivityQuery();
        activityQuery.setTypes(Collections.singleton(type));

        String noUserAgent = "";
        ActivityQueryResponse response = activityServiceController.query("userId", noUserAgent, activityQuery);
        assertEquals(activitySize, response.getCount());
        assertEquals(activitySize, response.getActivities().size());
        for (Activity activity : response.getActivities()) {
            assertEquals(type, activity.getType());
        }
    }

    @Test
    public void filterActivitiesByMultipleTypes() throws LockException {
        Set<String> types = Sets.newHashSet("badge", "balance-high");
        ActivityQuery activityQuery = new ActivityQuery();
        activityQuery.setTypes(types);

        String noUserAgent = "";
        ActivityQueryResponse response = activityServiceController.query("userId", noUserAgent, activityQuery);
        int expectedSize = 3;
        assertEquals(expectedSize, response.getCount());
        assertEquals(expectedSize, response.getActivities().size());

        assertThat(getActivityTypes(response.getActivities())).containsOnlyElementsOf(types);
    }

    @Test
    @Parameters({
            "2016-11-02, 2016-12-31, 0",
            "2016-09-02, , 2",
            ", 2016-10-02, 3",
            "2016-06-06, 2016-10-02, 3",
            "2016-10-02, 2016-06-06, 0",
    })
    public void filterActivitiesByDate(String fromDate, String toDate, int activitySize) throws LockException {
        Date startDate = parseDate(fromDate);
        Date endDate = parseDate(toDate);
        ActivityQuery activityQuery = new ActivityQuery();
        activityQuery.setStartDate(startDate);
        activityQuery.setEndDate(endDate);

        String noUserAgent = "";
        ActivityQueryResponse response = activityServiceController.query("userId", noUserAgent, activityQuery);
        assertEquals(activitySize, response.getCount());
        assertEquals(activitySize, response.getActivities().size());
        for (Activity activity : response.getActivities()) {
            assertTrue(isBetween(activity.getDate(), startDate, endDate));
        }
    }

    @Test
    public void filterActivitiesByUserAgent() throws LockException {
        ActivityQuery activityQuery = new ActivityQuery();

        ActivityQueryResponse response = activityServiceController
                .query("userId", "Tink Mobile/1.7.4 (iOS; 6.0, iPhone)", activityQuery);
        assertEquals(0, response.getCount());
        assertTrue(response.getActivities().isEmpty());
    }

    @Test
    public void receiveOneActivityOnQueryLimit() throws LockException {
        ActivityQuery activityQuery = new ActivityQuery();
        activityQuery.setLimit(1);

        String noUserAgent = "";
        ActivityQueryResponse response = activityServiceController.query("userId", noUserAgent, activityQuery);
        assertEquals(4, response.getCount());
        assertEquals(1, response.getActivities().size());
    }

    @Test
    public void doNotReceiveActivitiesForOffsetMoreThanElements() throws LockException {
        ActivityQuery activityQuery = new ActivityQuery();
        activityQuery.setOffset(10);

        String noUserAgent = "";
        ActivityQueryResponse response = activityServiceController.query("userId", noUserAgent, activityQuery);
        assertEquals(4, response.getCount());
        assertTrue(response.getActivities().isEmpty());
    }

    @Test(expected = LockException.class)
    public void throwExceptionOnExceptionOnLockWaitingForQueryActivities() throws LockException {
        when(statisticsActivitiesLock.waitForRead(anyLong(), any(TimeUnit.class))).thenThrow(new LockException());
        activityServiceController.query("userId", "", new ActivityQuery());
    }

    @Test
    public void returnActivitiesOnFalseLockWaiting() throws LockException {
        when(statisticsActivitiesLock.waitForRead(anyLong(), any(TimeUnit.class))).thenReturn(false);
        String noUserAgent = "";
        ActivityQueryResponse response = activityServiceController.query("userId", noUserAgent, new ActivityQuery());
        int expectedSize = 4;
        assertEquals(expectedSize, response.getCount());
        assertEquals(expectedSize, response.getActivities().size());
    }

    private boolean isBetween(Date date, Date startDate, Date endDate) {
        if (startDate != null && startDate.after(date)) {
            return false;
        }

        if (endDate != null && endDate.before(date)) {
            return false;
        }
        return true;
    }

    private Date parseDate(String dateString) {
        if (Strings.isNullOrEmpty(dateString)) {
            return null;
        } else {
            return DateUtils.parseDate(dateString);
        }
    }

    private Iterable<String> getActivityTypes(final Iterable<Activity> activities) {
        return FluentIterable
                .from(activities)
                .transform(Activity::getType)
                .toList();
    }

    private Activity createActivity(String activityId, String key, String type, Date date) {
        Activity activity = new Activity();
        activity.setId(activityId);
        activity.setKey(key);
        activity.setType(type);
        activity.setDate(date);
        activity.setMinAndroidVersion("5");
        activity.setMaxAndroidVersion("7");
        activity.setMinIosVersion("5");
        activity.setMaxIosVersion("7");

        return activity;
    }
}
