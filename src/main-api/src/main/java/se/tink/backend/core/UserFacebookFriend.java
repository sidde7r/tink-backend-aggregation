package se.tink.backend.core;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import se.tink.backend.utils.StringUtils;

@SuppressWarnings("serial")
@Entity
@Table(name = "users_facebook_friends")
public class UserFacebookFriend implements Serializable {
    @Id
    protected String id;
    protected String name;
    protected String profileId;
    protected String userId;

    public UserFacebookFriend() {
        id = StringUtils.generateUUID();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getProfileId() {
        return profileId;
    }

    public String getUserId() {
        return userId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
