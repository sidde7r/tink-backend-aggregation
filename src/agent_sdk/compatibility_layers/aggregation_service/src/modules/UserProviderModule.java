package src.agent_sdk.compatibility_layers.aggregation_service.src.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.util.Optional;
import se.tink.agent.sdk.operation.User;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.UserAvailability;
import src.agent_sdk.runtime.src.operation.UserImpl;

public class UserProviderModule extends AbstractModule {

    @Singleton
    @Provides
    public User provideUser(CredentialsRequest credentialsRequest) {
        return mapToUser(credentialsRequest);
    }

    public static User mapToUser(CredentialsRequest credentialsRequest) {
        if (credentialsRequest == null) {
            return null;
        }

        String locale =
                Optional.ofNullable(credentialsRequest.getUser())
                        .map(se.tink.libraries.user.rpc.User::getLocale)
                        .orElse(null);
        UserAvailability userAvailability = credentialsRequest.getUserAvailability();

        if (userAvailability == null) {
            return new UserImpl(false, false, null, locale);
        }

        return new UserImpl(
                userAvailability.isUserPresent(),
                userAvailability.isUserAvailableForInteraction(),
                userAvailability.getOriginatingUserIp(),
                locale);
    }
}
