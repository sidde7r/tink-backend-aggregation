package se.tink.backend.system.rpc;

import java.util.List;

import se.tink.backend.core.Activity;
import se.tink.backend.core.UserData;

public class ProcessFraudDataRequest {
    public static final String LOCK_PREFIX_USER_READ = "/locks/generateFraud/user/read/";
    public static final String LOCK_PREFIX_USER_WRITE = "/locks/generateFraud/user/write/";
    
    private UserData userData;
    private String userId;
    private List<Activity> activities;

    public UserData getUserData() {
        return userData;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserData(UserData userData) {
        this.userData = userData;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }
}
