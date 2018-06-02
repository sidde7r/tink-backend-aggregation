package se.tink.backend.main.controllers;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.common.config.FlagsConfiguration;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.controllers.AnalyticsController;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.common.utils.I18NUtils;
import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.core.StatisticMode;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import se.tink.backend.core.UserState;
import se.tink.backend.firehose.v1.queue.FirehoseQueueProducer;
import se.tink.backend.firehose.v1.rpc.FirehoseMessage;
import se.tink.backend.main.auth.session.UserSessionController;
import se.tink.backend.main.utils.UserAuthenticationMethodHelper;
import se.tink.backend.rpc.DeleteUserCommand;
import se.tink.backend.rpc.DeleteUserRequest;
import se.tink.backend.rpc.RateAppCommand;
import se.tink.backend.rpc.UserLogoutCommand;
import se.tink.backend.rpc.UserProfileResponse;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.utils.BeanUtils;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.Period;
import se.tink.libraries.date.ResolutionTypes;

public class UserServiceController {
    private static final LogUtils log = new LogUtils(UserServiceController.class);

    private final FirehoseQueueProducer firehoseQueueProducer;
    private final AnalyticsController analyticsController;
    private final AuthenticationServiceController authenticationServiceController;
    private final UserTrackerController userTrackerController;
    private final UserSessionController userSessionController;

    private final SystemServiceFactory systemServiceFactory;
    private final CacheClient cacheClient;

    private final UserRepository userRepository;
    private final UserStateRepository userStateRepository;

    private final ServiceConfiguration serviceConfiguration;
    private final UserAuthenticationMethodHelper userAuthenticationMethodHelper;

    @Inject
    public UserServiceController(FirehoseQueueProducer firehoseQueueProducer,
            AnalyticsController analyticsController,
            AuthenticationServiceController authenticationServiceController,
            UserTrackerController userTrackerController,
            UserSessionController userSessionController,
            SystemServiceFactory systemServiceFactory,
            CacheClient cacheClient,
            UserRepository userRepository,
            UserStateRepository userStateRepository,
            ServiceConfiguration serviceConfiguration,
            UserAuthenticationMethodHelper authenticationMethodHelper) {
        this.firehoseQueueProducer = firehoseQueueProducer;
        this.authenticationServiceController = authenticationServiceController;
        this.userSessionController = userSessionController;
        this.cacheClient = cacheClient;
        this.analyticsController = analyticsController;
        this.systemServiceFactory = systemServiceFactory;
        this.userRepository = userRepository;
        this.userStateRepository = userStateRepository;
        this.userTrackerController = userTrackerController;
        this.serviceConfiguration = serviceConfiguration;
        this.userAuthenticationMethodHelper = authenticationMethodHelper;
    }

    public List<Period> getPeriods(User user) {
        UserState userState = userStateRepository.findOneByUserId(user.getId());
        ResolutionTypes periodMode = user.getProfile().getPeriodMode();
        int periodBreakDate = user.getProfile().getPeriodAdjustedDay();

        List<Period> periods =
                userState.getPeriods() == null ? Lists.newArrayList() : Lists.newArrayList(userState.getPeriods());
        addPeriodsIfMissed(periods, periodMode, periodBreakDate);

        if (userState.getPeriods() == null || userState.getPeriods().size() != periods.size()) {
            userState.setPeriods(periods);
            userStateRepository.save(userState);
        }
        return periods;
    }

    private void addPeriodsIfMissed(List<Period> periods, ResolutionTypes periodMode, int periodBreakDate) {
        if (periods.isEmpty()) {
            periods.add(getCurrentPeriod(periodMode, periodBreakDate));
        }
        if (DateUtils.getCurrentPeriod(periods) == null) {
            String lastPeriod = periods.stream()
                    .max(Comparator.comparing(Period::getName))
                    .map(Period::getName)
                    .get();
            String currentPeriod = DateUtils.getCurrentMonthPeriod(periodMode, periodBreakDate);
            while (!Objects.equals(lastPeriod, currentPeriod)) {
                lastPeriod = DateUtils.getNextMonthPeriod(lastPeriod);
                periods.add(DateUtils.buildMonthlyPeriod(lastPeriod, periodMode, periodBreakDate));
            }
        }
    }

    private Period getCurrentPeriod(ResolutionTypes periodMode, int periodBreakDate) {
        return DateUtils
                .buildMonthlyPeriod(DateUtils.getCurrentMonthPeriod(periodMode, periodBreakDate), periodMode,
                        periodBreakDate);
    }

    public void delete(User user, DeleteUserCommand command) {
        authenticationServiceController.logout(user, new UserLogoutCommand(false, command.getHeaders()));

        // Expire all sessions.
        userSessionController.expireSessions(user.getId());

        log.info(user.getId(), "Deleting user: " + user.getUsername());

        analyticsController.trackUserEvent(user, "user.delete", command.getRemoteAddress());
        userTrackerController.trackUserDeleted(user);

        DeleteUserRequest deleteUserRequest = new DeleteUserRequest();
        deleteUserRequest.setComment(command.getComment());
        deleteUserRequest.setReasons(command.getReasons());
        deleteUserRequest.setUserId(user.getId());

        systemServiceFactory.getUpdateService().deleteUser(deleteUserRequest);
    }

    public UserProfile updateUserProfile(User user, UserProfile profile, Optional<String> remoteAddress)
            throws IllegalArgumentException {
        validateModifiableProfile(profile);

        if (Strings.isNullOrEmpty(profile.getLocale())) {
            profile.setLocale(I18NUtils.DEFAULT_LOCALE);
        }

        UserProfile existingProfile = user.getProfile();
        boolean configurationChanged = isConfigurationChanged(existingProfile, profile);

        BeanUtils.copyModifiableProperties(profile, existingProfile);

        userRepository.save(user);

        if (configurationChanged) {
            sendConfigurationToFirehose(user.getId(), user.getProfile());
        }

        analyticsController.trackUserEvent(user, "user.update-profile", remoteAddress);

        systemServiceFactory.getProcessService()
                .generateStatisticsAndActivitiesWithoutNotifications(user.getId(), StatisticMode.FULL);

        userStateRepository.updateContextTimestampByUserId(user.getId(), cacheClient);

        return existingProfile;
    }

    private boolean isConfigurationChanged(UserProfile oldProfile, UserProfile newProfile) {
        return !Objects.equals(oldProfile.getLocale(), newProfile.getLocale());
    }

    private void sendConfigurationToFirehose(String userId, UserProfile userProfile) {
        firehoseQueueProducer.sendUserConfigurationMessage(userId, FirehoseMessage.Type.UPDATE, null, userProfile);
    }

    private void validateModifiableProfile(UserProfile profile) throws IllegalArgumentException {
        if (profile == null) {
            throw new IllegalArgumentException();
        }

        if (profile.getBirth() != null && !UserProfile.PATTERN_BIRTH.matcher(profile.getBirth()).matches()) {
            throw new IllegalArgumentException("Incorrect birth date.");
        }

        if (profile.getGender() != null && !UserProfile.PATTERN_GENDER.matcher(profile.getGender()).matches()) {
            throw new IllegalArgumentException("Incorrect gender.");
        }
    }

    public List<String> generateDynamicFlags(User user) {
        Set<String> dynamicFlags = Sets.newHashSet();
        Set<String> existingFlags = Sets.newHashSet(user.getFlags());

        FlagsConfiguration flagsConfiguration = serviceConfiguration.getFlags();

        Map<String, List<String>> dynamicInheritenceFlags = flagsConfiguration.getDynamicInheritance();

        if (dynamicInheritenceFlags == null) {
            return Collections.emptyList();
        }

        for (String flag : existingFlags) {
            if (dynamicInheritenceFlags.containsKey(flag)) {
                for (String dynamicFlag : dynamicInheritenceFlags.get(flag)) {
                    if (!existingFlags.contains(dynamicFlag) && !dynamicFlags.contains(dynamicFlag)) {
                        dynamicFlags.add(dynamicFlag);
                    }
                }
            }
        }

        return Lists.newArrayList(dynamicFlags);
    }

    public void rateApp(RateAppCommand command) {
        UserState userState = userStateRepository.findOneByUserId(command.getUserId());
        userState.setRateThisAppStatus(command.getStatus());
        userStateRepository.save(userState);
        systemServiceFactory.getProcessService()
                .generateStatisticsAndActivitiesWithoutNotifications(command.getUserId(), StatisticMode.SIMPLE);
    }

    public UserProfileResponse getUserProfile(User user) {
        UserProfileResponse response = new UserProfileResponse();

        response.setUsername(user.getUsername());
        response.setNationalId(user.getNationalId());
        response.setCreated(user.getCreated());
        response.setAvailableLoginMethods(
                userAuthenticationMethodHelper.getAvailableLoginMethods(user));
        response.setAuthorizedLoginMethods(
                userAuthenticationMethodHelper.getAuthorizedLoginMethods(user));
        return response;
    }
}
