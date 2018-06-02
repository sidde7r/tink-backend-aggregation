package se.tink.backend.main.controllers;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.common.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.common.controllers.AnalyticsController;
import se.tink.backend.common.repository.mysql.main.UserAdvertiserIdRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.common.rpc.UserPropertiesBuilderCommand;
import se.tink.backend.common.tracking.EventTracker;
import se.tink.backend.common.tracking.TrackableEvent;
import se.tink.backend.common.tracking.appsflyer.AppsFlyerEventBuilder;
import se.tink.backend.common.tracking.appsflyer.AppsFlyerTracker;
import se.tink.backend.common.utils.UserPropertiesBuilder;
import se.tink.backend.core.User;
import se.tink.backend.core.UserAdvertiserId;
import se.tink.backend.core.UserOrigin;
import se.tink.backend.core.UserState;
import se.tink.backend.utils.LogUtils;

public class UserTrackerController {
    private static final LogUtils log = new LogUtils(UserTrackerController.class);
    private final AnalyticsController analyticsController;
    private final EventTracker eventTracker;
    private final AppsFlyerTracker appsFlyerTracker;
    private final UserPropertiesBuilder userPropertiesBuilder;
    private UserAdvertiserIdRepository userAdvertiserIdRepository;
    private UserStateRepository userStateRepository;
    private final ListenableThreadPoolExecutor<Runnable> trackingExecutor;

    @Inject
    public UserTrackerController(AnalyticsController analyticsController, EventTracker eventTracker,
            AppsFlyerTracker appsFlyerTracker,
            UserPropertiesBuilder userPropertiesBuilder,
            UserAdvertiserIdRepository userAdvertiserIdRepository,
            UserStateRepository userStateRepository,
            @Named("trackingExecutor") ListenableThreadPoolExecutor<Runnable> trackingExecutor) {
        this.analyticsController = analyticsController;
        this.eventTracker = eventTracker;
        this.appsFlyerTracker = appsFlyerTracker;
        this.userPropertiesBuilder = userPropertiesBuilder;
        this.userAdvertiserIdRepository = userAdvertiserIdRepository;
        this.userStateRepository = userStateRepository;
        this.trackingExecutor = trackingExecutor;
    }

    public void identify(User user, UserOrigin userOrigin, String userAgent, Optional<String> remoteAddress) {
        identify(user, userPropertiesBuilder
                .build(new UserPropertiesBuilderCommand(user, userOrigin, userAgent, remoteAddress)));
    }

    public void identify(User user, String userAgent, Optional<String> remoteAddress) {
        identify(user, userPropertiesBuilder
                .build(new UserPropertiesBuilderCommand(user, userAgent, remoteAddress)));
    }

    private void identify(User user, final Map<String, Object> properties) {
        if (!user.isTrackingEnabled() || properties == null) {
            return;
        }

        analyticsController.trackUserProperties(user, properties);
    }

    public void trackUserDeleted(User user) {
        if (!user.isTrackingEnabled()) {
            return;
        }

        try {

            Map<String, Object> properties = Maps.newHashMap();
            properties.put("Deleted", true);

            TrackableEvent event = TrackableEvent.userProperties(user.getId(), properties);

            eventTracker.trackUserProperties(event);

        } catch (Exception e) {
            log.error(user.getId(), "Could not track event", e);
        }
    }

    public void trackUserRegistered(User user, UserOrigin userOrigin, String userAgent,
            Optional<String> remoteAddress) {
        if (!user.isTrackingEnabled()) {
            return;
        }

        identify(user, userOrigin, userAgent, remoteAddress);

        analyticsController.trackUserEvent(user, "user.register", remoteAddress);

        // Track back to AppsFlyer for follow-up metrics
        if (userOrigin != null && UserOrigin.SERVICE_NAME_APPSFLYER.equals(userOrigin.getServiceName())) {

            String deviceType = "";
            if (!Strings.isNullOrEmpty(userOrigin.getDeviceType())) {
                deviceType = userOrigin.getDeviceType().toLowerCase();
            }

            if (Objects.equals("ios", deviceType) || Objects.equals("android", deviceType)) {
                trackingExecutor.execute(() -> {
                    AppsFlyerEventBuilder appsFlyerEvent = AppsFlyerEventBuilder.client(userOrigin.getDeviceType(),
                            userOrigin.getExternalServiceId());

                    appsFlyerTracker.trackEvent(appsFlyerEvent.registered().build());
                });
            }
        }
    }

    public void registerAdvertiserId(String userId, final String advertiserId, final boolean limited,
            String deviceType) {
        Preconditions.checkNotNull(userId);

        List<UserAdvertiserId> advertiserIds = userAdvertiserIdRepository.findByUserId(userId);

        if (Strings.isNullOrEmpty(advertiserId) || !limited) {
            return;
        }

        Optional<UserAdvertiserId> existingIdfa = advertiserIds.stream()
                .filter(userIdfa -> Objects.equals(advertiserId, userIdfa.getAdvertiserId()))
                .findFirst();

        UserAdvertiserId userAdvertiserId;
        if (existingIdfa.isPresent()) {
            userAdvertiserId = existingIdfa.get();
        } else {
            userAdvertiserId = new UserAdvertiserId(userId);
            userAdvertiserId.setAdvertiserId(advertiserId);
            userAdvertiserId.setDeviceType(deviceType);
        }
        userAdvertiserId.setLimitted(limited);
        userAdvertiserId.setUpdated(new Date());
        userAdvertiserIdRepository.save(userAdvertiserId);
    }

    public void updateLastLogin(String userId) {
        Preconditions.checkNotNull(userId);

        UserState state = userStateRepository.findOneByUserId(userId);

        if (state == null) {
            log.error(userId, "Could not fetch user state.");
            return;
        }

        state.setLastLogin(new Date());

        userStateRepository.save(state);
    }


}
