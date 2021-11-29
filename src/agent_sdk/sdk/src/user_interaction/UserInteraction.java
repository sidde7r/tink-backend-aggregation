package se.tink.agent.sdk.user_interaction;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.Optional;
import javax.annotation.Nullable;

public class UserInteraction<T> {
    private final T payload;
    private final boolean userResponseRequired;
    @Nullable private final String customResponseKey;

    UserInteraction(T payload, boolean userResponseRequired, @Nullable String customResponseKey) {
        this.payload =
                Preconditions.checkNotNull(payload, "UserInteraction payload cannot be null.");
        this.userResponseRequired = userResponseRequired;
        this.customResponseKey = customResponseKey;
    }

    public T getPayload() {
        return payload;
    }

    public boolean isUserResponseRequired() {
        return userResponseRequired;
    }

    public Optional<String> getCustomResponseKey() {
        return Optional.ofNullable(customResponseKey);
    }

    public static UserInteractionBuilder<ThirdPartyAppInfo> thirdPartyApp(
            ThirdPartyAppInfo appInfo) {
        return new UserInteractionBuilder<>(appInfo);
    }

    // TODO: fields type instead of String.
    public static UserInteractionBuilder<ImmutableList<String>> supplementalInformation(
            ImmutableList<String> fields) {
        return new UserInteractionBuilder<>(fields);
    }
}
