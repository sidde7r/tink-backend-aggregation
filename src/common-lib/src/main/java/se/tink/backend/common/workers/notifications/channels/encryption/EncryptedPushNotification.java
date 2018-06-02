package se.tink.backend.common.workers.notifications.channels.encryption;


public class EncryptedPushNotification {

    private String id;
    private String title;
    private String message;
    private String url;
    
    public String getId() {
        return id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
}
