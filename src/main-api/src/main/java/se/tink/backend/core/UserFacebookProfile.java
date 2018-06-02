package se.tink.backend.core;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Type;

@SuppressWarnings("serial")
@Entity
@Table(name = "users_facebook_profiles")
public class UserFacebookProfile implements Serializable {
    @Type(type = "text")
    private String accessToken;
    private Date birthday;
    private String email;
    private String firstName;
    private String gender;
    private String lastName;
    private String locationId;
    private String locationName;
    private String profileId;
    @Enumerated(EnumType.STRING)
    private UserConnectedServiceStates state;
    private Date updated;
    @Id
    protected String userId;

    public UserFacebookProfile() {

    }

    public UserFacebookProfile(String userId, String profileId) {
        this.userId = userId;
        this.profileId = profileId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public Date getBirthday() {
        return birthday;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getGender() {
        return gender;
    }

    public String getLastName() {
        return lastName;
    }

    public String getLocationId() {
        return locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public String getProfileId() {
        return profileId;
    }

    public UserConnectedServiceStates getState() {
        return state;
    }

    public Date getUpdated() {
        return updated;
    }

    public String getUserId() {
        return userId;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public void setState(UserConnectedServiceStates state) {
        this.state = state;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
