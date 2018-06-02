package se.tink.backend.common.workers.notifications.channels.encryption;

public class EncryptedPushNotificationContainer {
    private String key;
    private String payload;
    
    public void setKey(String key) {
        this.key = key;
    }
    
    public void setPayload(String payload) {
        this.payload = payload;
    }
    
    public String getKey() {
        return key;
    }
    
    public String getPayload() {
        return payload;
    }
}
