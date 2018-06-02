package se.tink.backend.common.resources;

import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Optional;
import com.google.inject.Inject;
import se.tink.backend.common.config.AuthenticationConfiguration;
import se.tink.backend.common.repository.mysql.main.UserEventRepository;
import se.tink.backend.core.UserEvent;
import se.tink.backend.core.UserEventTypes;

public class UserEventHelper {
    private final UserEventRepository userEventRepository;
    private final int authenticationErrorThreshold;

    // Default value is set in AuthenticationConfiguration. This is just here to prevent bad configurations
    private static final long AUTHENTICATION_ERROR_BLOCK_UPPER_LIMIT = 15;

    @Inject
    public UserEventHelper(UserEventRepository userEventRepository,
            AuthenticationConfiguration authenticationConfiguration) {
        Preconditions.checkState(authenticationConfiguration.getAuthenticationErrorThreshold() <=
                AUTHENTICATION_ERROR_BLOCK_UPPER_LIMIT, "Authentication error threshold mustn't be above 15");
        authenticationErrorThreshold = authenticationConfiguration.getAuthenticationErrorThreshold();

        this.userEventRepository = userEventRepository;
    }

    public void save(String userId, UserEventTypes type, Optional<String> remoteAddress) {
        UserEvent userEvent = new UserEvent();

        userEvent.setType(type);
        userEvent.setUserId(userId);
        remoteAddress.ifPresent(userEvent::setRemoteAddress);

        userEventRepository.save(userEvent);
    }

    public void save(String userId, UserEventTypes type, String remoteAddress) {
        save(userId, type, Optional.of(remoteAddress));
    }

    /**
     * Check if a user has tried to log in too many times recently
     *
     * @param userId of the user to check
     * @return true if the user has tried logging in too many times recently, otherwise false
     */
    public boolean shouldBlockUser(String userId) {
        long recentAuthenticationErrors = getRecentAuthenticationErrors(userId);

        return recentAuthenticationErrors >= authenticationErrorThreshold;
    }

    private long getRecentAuthenticationErrors(String userId) {
        List<UserEvent> userEvents = userEventRepository.findMostRecentByUserId(userId);

        int numberOfConsecutiveAuthenticationErrors = 0;

        for (UserEvent userEvent : userEvents) {
            if (userEvent.getType() == UserEventTypes.AUTHENTICATION_ERROR) {
                numberOfConsecutiveAuthenticationErrors++;
            } else {
                break;
            }
        }

        return numberOfConsecutiveAuthenticationErrors;
    }
}
