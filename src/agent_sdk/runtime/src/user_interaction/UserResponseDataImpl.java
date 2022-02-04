package se.tink.agent.runtime.user_interaction;

import com.google.common.base.Strings;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import se.tink.agent.sdk.user_interaction.UserResponseData;

public class UserResponseDataImpl implements UserResponseData {
    private final Map<String, String> data;

    public UserResponseDataImpl(Map<String, String> data) {
        this.data = data;
    }

    @Override
    public Optional<String> tryGet(String key) {
        if (Objects.isNull(this.data)) {
            return Optional.empty();
        }

        String value = this.data.get(key);
        if (Strings.isNullOrEmpty(value)) {
            return Optional.empty();
        }

        return Optional.of(value);
    }
}
