package se.tink.backend.webhook.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import se.tink.backend.firehose.v1.models.Credential;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebhookCredentials {
    private String id;
    private String userId;
    private String status;
    private String statusPayload;
    private long statusUpdated;

    public static WebhookCredentials fromFirehoseCredentials(Credential credential) {
        WebhookCredentials webhookCredentials = new WebhookCredentials();
        webhookCredentials.id = credential.getId();
        webhookCredentials.userId = credential.getUserId();
        webhookCredentials.status = credential.getStatus().toString();
        webhookCredentials.statusPayload = credential.getStatusPayload();
        webhookCredentials.statusUpdated = credential.getStatusUpdated();
        return webhookCredentials;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getStatus() {
        return status;
    }

    public String getStatusPayload() {
        return statusPayload;
    }

    public long getStatusUpdated() {
        return statusUpdated;
    }
}
