package se.tink.backend.common.mail.monthly.summary.model;

import java.util.Date;

public class EmptyEmailContent {
    private String locale;
    private String userId;
    private String unsubscribeToken;
    private Date startDate;
    private Date endDate;
    private boolean androidUser;

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUnsubscribeToken() {
        return unsubscribeToken;
    }

    public void setUnsubscribeToken(String unsubscribeToken) {
        this.unsubscribeToken = unsubscribeToken;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public boolean isAndroidUser() {
        return androidUser;
    }

    public void setAndroidUser(boolean androidUser) {
        this.androidUser = androidUser;
    }
}
