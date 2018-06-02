package se.tink.backend.webhook.rpc;

import se.tink.backend.firehose.v1.models.Activity;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class WebhookActivity {

    private String id;
    private String key;
    private String message;
    private String title;
    private String type;
    private String userId;
    private double importance;
    private long date;
    private Object content;

    public static WebhookActivity fromFirehoseActivity(Activity activity) {
        WebhookActivity webhookActivity = new WebhookActivity();

        webhookActivity.setId(activity.getId());
        webhookActivity.setKey(activity.getKey());
        webhookActivity.setMessage(activity.getMessage());
        webhookActivity.setTitle(activity.getTitle());
        webhookActivity.setType(activity.getType());
        webhookActivity.setUserId(activity.getUserId());
        webhookActivity.setImportance(activity.getImportance());
        webhookActivity.setDate(activity.getDate());
        webhookActivity.setContent(SerializationUtils.deserializeFromString(activity.getContent(), Object.class));

        return webhookActivity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getImportance() {
        return importance;
    }

    public void setImportance(double importance) {
        this.importance = importance;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }
}
