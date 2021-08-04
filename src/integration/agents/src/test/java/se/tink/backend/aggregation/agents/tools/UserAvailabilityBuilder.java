package se.tink.backend.aggregation.agents.tools;

import lombok.Builder;
import org.junit.Ignore;
import se.tink.libraries.credentials.service.UserAvailability;

@Builder
@Ignore
public class UserAvailabilityBuilder {

    @Builder.Default private boolean userPresent = true;
    @Builder.Default private boolean userAvailableForInteraction = true;
    @Builder.Default private String originatingUserIp = "124.211.203.122";

    public static UserAvailability availableUser() {
        return UserAvailabilityBuilder.builder().build().getUserAvailability();
    }

    public UserAvailability getUserAvailability() {
        UserAvailability userAvailability = new UserAvailability();
        userAvailability.setUserPresent(userPresent);
        userAvailability.setUserAvailableForInteraction(userAvailableForInteraction);
        userAvailability.setOriginatingUserIp(originatingUserIp);
        return userAvailability;
    }
}
