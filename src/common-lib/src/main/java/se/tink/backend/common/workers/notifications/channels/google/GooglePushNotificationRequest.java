package se.tink.backend.common.workers.notifications.channels.google;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import se.tink.backend.core.Device;

public class GooglePushNotificationRequest implements Serializable {

    public static class Keys {
        public static String ENCRYPTED_CONTENT = "encrypted-content";
        public static String KEY = "key";
        public static String TITLE = "title";
        public static String URL = "url";
        public static String TYPE = "type";
        public static String MESSAGE = "message";
    }

    public GooglePushNotificationRequest() {
        data = Maps.newHashMap();
        pushTokens = Lists.newArrayList();
    }

    @JsonProperty("registration_ids")
    private List<String> pushTokens;
    private Map<String, String> data;

    @JsonIgnore
    private List<Device> devices;

    public Map<String, String> getData() {
        return data;
    }

    public void setMessage(String message) {
        if (!Strings.isNullOrEmpty(message)) {
            data.put(Keys.MESSAGE, message);
        }
    }

    public void setType(String type) {
        if (!Strings.isNullOrEmpty(type)) {
            data.put(Keys.TYPE, type);
        }
    }

    public void setTitle(String title) {
        if (!Strings.isNullOrEmpty(title)) {
            data.put(Keys.TITLE, title);
        }
    }

    public void setUrl(String url) {
        if (!Strings.isNullOrEmpty(url)) {
            data.put(Keys.URL, url);
        }
    }

    public void setKey(String key) {
        if (!Strings.isNullOrEmpty(key)) {
            data.put(Keys.KEY, key);
        }
    }

    public void setEncryptedContent(String content) {
        if (!Strings.isNullOrEmpty(content)) {
            data.put(Keys.ENCRYPTED_CONTENT, content);
        }
    }

    public static GooglePushNotificationRequestBuilder builder() {
        return new GooglePushNotificationRequestBuilder();
    }

    public List<String> getPushTokens() {
        return pushTokens;
    }

    public void setPushTokens(List<String> pushTokens) {
        this.pushTokens = pushTokens;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices;
    }

    public List<Device> getDevices() {
        return devices;
    }
}
