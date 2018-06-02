package se.tink.backend.rpc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;

public class RegisterUserPushTokenCommand {
    private String userId;
    private String userAgent;
    private String notificationToken;
    private String notificationPublicKey;
    private String deviceId;

    public String getUserId() {
        return userId;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getNotificationToken() {
        return notificationToken;
    }

    public String getNotificationPublicKey() {
        return notificationPublicKey;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public final static class Builder {
        private String userId;
        private String userAgent;
        private String notificationToken;
        private String notificationPublicKey;
        private String deviceId;

        public Builder withUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder withUserAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder withNotificationToken(String notificationToken) {
            this.notificationToken = notificationToken;
            return this;
        }

        public Builder withNotificationPublicKey(String notificationPublicKey) {
            this.notificationPublicKey = notificationPublicKey;
            return this;
        }

        public Builder withDeviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public RegisterUserPushTokenCommand build() {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(userId), "Invalid user id.");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(userAgent), "Invalid user agent.");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(notificationToken), "Invalid notification token.");

            RegisterUserPushTokenCommand command = new RegisterUserPushTokenCommand();
            command.userId = userId;
            command.userAgent = userAgent;
            command.notificationToken = notificationToken;
            command.deviceId = deviceId;

            if (Strings.isNullOrEmpty(notificationPublicKey)) {
                command.notificationPublicKey = null;
            } else {
                // Delete all whitespaces and new lines from the notification public key
                command.notificationPublicKey = StringUtils.deleteWhitespace(notificationPublicKey);
            }

            return command;
        }
    }
}
