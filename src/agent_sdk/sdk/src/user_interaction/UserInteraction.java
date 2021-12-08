package se.tink.agent.sdk.user_interaction;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.Optional;
import javax.annotation.Nullable;
import se.tink.backend.agents.rpc.Field;

public class UserInteraction<T> {
    private final UserInteractionType type;
    private final T payload;
    private final boolean userResponseRequired;
    @Nullable private final String customResponseKey;

    UserInteraction(
            UserInteractionType type,
            T payload,
            boolean userResponseRequired,
            @Nullable String customResponseKey) {
        this.type = type;
        this.payload =
                Preconditions.checkNotNull(payload, "UserInteraction payload cannot be null.");
        this.userResponseRequired = userResponseRequired;
        this.customResponseKey = customResponseKey;
    }

    public UserInteractionType getType() {
        return type;
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
        return new UserInteractionBuilder<>(UserInteractionType.THIRD_PARTY_APP, appInfo);
    }

    public static UserInteractionBuilder<ImmutableList<Field>> supplementalInformation(
            ImmutableList<Field> fields) {
        return new UserInteractionBuilder<>(UserInteractionType.SUPPLEMENTAL_INFORMATION, fields);
    }

    public static UserInteraction<String> swedishMobileBankId(@Nullable String autostartToken) {
        return new UserInteractionBuilder<>(
                        UserInteractionType.SWEDISH_MOBILE_BANKID, autostartToken)
                .build();
    }
}
