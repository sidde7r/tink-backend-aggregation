package se.tink.backend.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "users_push_tokens")
public class Device {
    @ApiModelProperty(value = "Date when the device was first created", example="1455740874875")
    private Date created;
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Creatable
    @Modifiable
    @ApiModelProperty(value = "The APN or GCM token", example = "LRJ2bFzHA1jUIkwayDqxteNsWY3udejkEe9UwRMt12E_R5i...", required=true)
    @Column(name="`token`")
    private String notificationToken;
    @Creatable
    @Modifiable
    @Column(name="`type`")
    @ApiModelProperty(value = "The operating system of the device", example = "android", required=true)
    private String type;
    @ApiModelProperty(value = "Date when the device was last updated", example="1455740874875")
    private Date updated;
    @ApiModelProperty(name = "userId", hidden = true)
    private String userId;
    @Creatable
    @Modifiable
    @Type(type = "text")
    @ApiModelProperty(value = "Public key for sending encrypted push notifications to the device", example="MIIC7DCCAdQCAQEwDQYJKoZIhvcNAQELBQAwPDE6MDgGA1UEAwwxTXlTUUxfU2Vy...")
    private String publicKey;
    @Creatable
    @Modifiable
    @ApiModelProperty(value = "The app ID of the app that registers the device", example = "se.tink.android", required=true)
    private String appId;
    @ApiModelProperty(value = "A device token, should be unique per device and app", required=true, example="51ed6c20-dad6-4270-a4f2-56648f442047")
    private String deviceToken;
    @Creatable
    @Modifiable
    @ApiModelProperty(value = "The User-Agent of the device", example = "Tink Mobile/1.7.8 (Android; 4.4.2, LGE Nexus 4)", required=true)
    private String userAgent;
    @Creatable
    @Modifiable
    @ApiModelProperty(value = "The device name", example = "Fredrik's iPhone", required=true)
    private String deviceName;

    public String getDeviceName() { return deviceName; }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Device() {
        created = new Date();
    }

    public String getNotificationToken() {
        return notificationToken;
    }

    public void setNotificationToken(String notificationToken) {
        this.notificationToken = notificationToken;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Date getCreated() {
        return this.created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPublicKey() {
        return publicKey;
    }
    
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    @Override
    public String toString() {
        String trimmedToken = null;
        if (notificationToken != null) {
            // Trimming token to avoid logging the full token somewhere.
            trimmedToken = notificationToken.length() > 5 ? notificationToken.substring(0, 5) : notificationToken;
        }

        return MoreObjects.toStringHelper(this).add("userId", userId).add("UserAgent", userAgent).add("type", type)
                .add("notificationToken", trimmedToken)
                .toString();
    }

}
