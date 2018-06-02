package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;
import se.tink.backend.core.User;

public class NotificationsConfiguration {
    @JsonProperty
    private String disabledGenerators;
    @JsonProperty
    private int startHourOfDay;
    @JsonProperty
    private int endHourOfDay;
    @JsonProperty
    private boolean enabled = true;
    @JsonProperty
    private int maxAgeDays = 1;
    @JsonProperty
    private Map<String, NotificationsApplicationConfiguration> applications = Maps.newHashMap();
    @JsonProperty
    private boolean shouldGroupNotifications = true;
    @JsonProperty
    private String deepLinkPrefix = "tink://";

    public int getStartHourOfDay() {
        return startHourOfDay;
    }

    public int getEndHourOfDay() {
        return endHourOfDay;
    }

    public String getDisabledGenerators() {
        return disabledGenerators;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getMaxAgeDays() {
        return maxAgeDays;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, NotificationsApplicationConfiguration> getApplications() {
        return applications;
    }

    public boolean shouldSendNotifications(User user) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(user.getProfile().getTimeZone()));
        return shouldSendNotifications(calendar.get(Calendar.HOUR_OF_DAY));
    }

    boolean shouldSendNotifications(int hourOfDay) {
        return (hourOfDay >= startHourOfDay && hourOfDay < endHourOfDay);
    }

    public void setStartHourOfDay(int startHourOfDay) {
        this.startHourOfDay = startHourOfDay;
    }

    public void setEndHourOfDay(int endHourOfDay) {
        this.endHourOfDay = endHourOfDay;
    }

    public boolean shouldGroupNotifications() {
        return shouldGroupNotifications;
    }

    public String getDeepLinkPrefix() {
        return deepLinkPrefix;
    }

    public void setDeepLinkPrefix(String deepLinkPrefix) {
        this.deepLinkPrefix = deepLinkPrefix;
    }
}
