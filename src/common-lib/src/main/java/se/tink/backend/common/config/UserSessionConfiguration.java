package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.joda.time.Period;
import se.tink.backend.core.SessionTypes;

public class UserSessionConfiguration {
    @JsonProperty
    private Period defaultTimeout = Period.minutes(30);

    @JsonProperty
    private Map<SessionTypes, Period> timeoutByType = ImmutableMap.of(SessionTypes.MOBILE, Period.ZERO);

    public Period getDefaultTimeout() {
        return defaultTimeout;
    }

    public Optional<Period> getTimeoutByType(SessionTypes type) {
        return timeoutByType == null ? Optional.empty() : Optional.ofNullable(timeoutByType.get(type));
    }

    public Period getTimeoutByTypeOrDefault(SessionTypes type) {
        return getTimeoutByType(type).orElse(defaultTimeout);
    }
}
