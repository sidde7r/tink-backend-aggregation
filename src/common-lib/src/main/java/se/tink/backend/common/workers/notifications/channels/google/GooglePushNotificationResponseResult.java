package se.tink.backend.common.workers.notifications.channels.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GooglePushNotificationResponseResult {

    @JsonProperty("message_id")
    private String messageId;

    @JsonProperty("registration_id")
    private String token;

    private String error;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getError() {
        return error;
    }

    public boolean hasError() {
        return !Strings.isNullOrEmpty(error);
    }

    public boolean isInvalidToken() {
        return !Strings.isNullOrEmpty(token);
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("messageId", messageId).add("token", token)
                .add("error", error).toString();
    }
}
