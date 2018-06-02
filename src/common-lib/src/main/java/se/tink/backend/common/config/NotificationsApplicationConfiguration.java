package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NotificationsApplicationConfiguration {
    @JsonProperty
    AppleNotificationConfiguration apple;
    @JsonProperty
    GoogleNotificationConfiguration google;

    public AppleNotificationConfiguration getApple() {
        return apple;
    }

    public GoogleNotificationConfiguration getGoogle() {
        return google;
    }
}
