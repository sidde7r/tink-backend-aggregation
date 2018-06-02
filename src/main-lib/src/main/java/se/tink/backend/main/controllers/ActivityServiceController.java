package se.tink.backend.main.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.common.concurrency.LockFactory;
import se.tink.backend.common.concurrency.StatisticsActivitiesLock;
import se.tink.backend.common.dao.ActivityDao;
import se.tink.backend.common.exceptions.LockException;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.MeterFactory;
import se.tink.libraries.metrics.MetricId;
import se.tink.backend.common.repository.mysql.main.FeedbackRepository;
import se.tink.backend.core.Activity;
import se.tink.backend.core.TinkUserAgent;
import se.tink.backend.rpc.ActivityQuery;
import se.tink.backend.rpc.ActivityQueryResponse;
import se.tink.backend.rpc.Feedback;
import se.tink.backend.utils.LogUtils;

public class ActivityServiceController {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int ACTIVITIES_WAIT_FOR_READ_TIME_SEC = 5;
    private static final LogUtils log = new LogUtils(ActivityServiceController.class);
    private final ActivityDao activityDao;
    private final FeedbackRepository feedbackRepository;
    private final LockFactory lockFactory;
    private final Counter activityRequestNoTimeoutCounter;
    private final Counter activityRequestTimeoutCounter;

    @Inject
    public ActivityServiceController(ActivityDao activityDao, FeedbackRepository feedbackRepository, LockFactory lockFactory,
            MeterFactory meterFactory) {
        this.activityDao = activityDao;
        this.feedbackRepository = feedbackRepository;
        this.lockFactory = lockFactory;

        this.activityRequestTimeoutCounter = meterFactory.getCounter(
                MetricId.newId("activity_request").label("timeout", "yes"));
        this.activityRequestNoTimeoutCounter = meterFactory.getCounter(
                MetricId.newId("activity_request").label("timeout", "no"));
    }

    public void feedback(String userId, final String activityId, String opinion) throws NoSuchElementException {
        List<Activity> activities = activityDao.findByUserId(userId);

        Activity activity = Iterables.find(activities, a -> (Objects.equal(a.getId(), activityId)));

        Feedback feedback = new Feedback();

        feedback.setObjectId(activityId);
        feedback.setObjectType(Feedback.ObjectTypes.ACTIVITY);
        feedback.setOpinion(opinion);

        try {
            feedback.setPayload(MAPPER.writeValueAsString(activity));
        } catch (JsonProcessingException e) {
            log.error(userId, activityId, "Cannot convert activity to Json", e);
        }

        feedbackRepository.save(feedback);
    }

    public ActivityQueryResponse query(String userId, String userAgent,
            final ActivityQuery query) throws LockException {
        waitForFreshActivities(userId);

        final Predicate<Activity> userAgentPredicate = buildUserAgentPredicate(userAgent);

        FluentIterable<Activity> activities = FluentIterable.from(activityDao.findByUserId(userId))
                .filter(activity -> {
                    if (query.getTypes() != null && !query.getTypes().isEmpty() && !query.getTypes()
                            .contains(activity.getType())) {
                        return false;
                    }

                    if (query.getStartDate() != null && query.getStartDate().after(activity.getDate())) {
                        return false;
                    }

                    if (query.getEndDate() != null && query.getEndDate().before(activity.getDate())) {
                        return false;
                    }

                    if (!userAgentPredicate.apply(activity)) {
                        return false;
                    }

                    return true;
                });

        ActivityQueryResponse response = new ActivityQueryResponse();

        response.setCount(activities.size());

        if (query.getLimit() != 0) {
            response.setActivities(activities.skip(query.getOffset()).limit(query.getLimit()).toList());
        } else {
            response.setActivities(activities.skip(query.getOffset()).toList());
        }

        return response;
    }

    private Predicate<Activity> buildUserAgentPredicate(String userAgentString) {
        final TinkUserAgent userAgent = new TinkUserAgent(userAgentString);

        return a -> (userAgent.hasValidVersion(a.getMinIosVersion(), a.getMaxIosVersion(), a.getMinAndroidVersion(),
                a.getMaxAndroidVersion()));
    }

    private void waitForFreshActivities(String userId) throws LockException {
        StatisticsActivitiesLock lock = lockFactory.getStatisticsAndActivitiesLock(userId);

        try {
            // If we wait more than 5 seconds we are okay with returning a stale HTML view. This will lead to feed
            // flickering, but client will update activities anyway when context has been updated.
            if (!lock.waitForRead(ACTIVITIES_WAIT_FOR_READ_TIME_SEC, TimeUnit.SECONDS)) {
                log.info(userId, "Timeout while waiting for generating activities");
                activityRequestTimeoutCounter.inc();
            } else {
                activityRequestNoTimeoutCounter.inc();
            }
        } catch (LockException e) {
            log.error(userId, "Could not wait for fresh activities", e);
            throw e;
        }
    }

    public Activity get(String userId, String userAgent, final String key) throws NoSuchElementException,
            LockException {
        waitForFreshActivities(userId);

        final Predicate<Activity> userAgentPredicate = buildUserAgentPredicate(userAgent);

        Optional<Activity> activity = Optional.ofNullable(FluentIterable
                .from(activityDao.findByUserId(userId))
                .filter(activity1 -> {
                    if (!Objects.equal(key, activity1.getKey())) {
                        return false;
                    }

                    if (!userAgentPredicate.apply(activity1)) {
                        return false;
                    }

                    return true;
                })
                .first().orNull());

        if (!activity.isPresent()) {
            throw new NoSuchElementException();
        }

        return activity.get();
    }
}
