package se.tink.libraries.user.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.Period;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.libraries.serialization.utils.SerializationUtils;

@SuppressWarnings("serial")
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserProfile implements Serializable {
    public static final Pattern PATTERN_BIRTH = Pattern.compile("([0-9]{4})(-([0-9]{2})(-([0-9]{2}))?)?");

    private String birth; // Should be on form yyyy, yyyy-MM or yyyy-MM-dd
    private String currency;
    private String gender;
    private String locale;
    private String market;
    @JsonProperty("notificationSettings")
    private NotificationSettings notificationSettings;
    private int periodAdjustedDay;
    private ResolutionTypes periodMode;
    private String timeZone;
    private String fraudPersonNumber;
    private String name;
    private boolean cashbackEnabled = false;

    public String getBirth() {
        return birth;
    }

    @JsonIgnore
    public int getBirthYear() {
        if (birth != null) {
            Matcher m = PATTERN_BIRTH.matcher(birth);
            if (m.matches()) {
                return Integer.parseInt(m.group(1));
            }
        }

        return 0; // Jesus
    }

    public String getCurrency() {
        return currency;
    }

    public String getGender() {
        return gender;
    }

    public String getLocale() {
        return locale;
    }

    public String getMarket() {
        return market;
    }

    public String getName() {
        return name;
    }

    public NotificationSettings getNotificationSettings() {
        return notificationSettings;
    }

    @JsonIgnore
    public String getNotificationSettingsSerialized() {
        return SerializationUtils.serializeToString(notificationSettings);
    }

    public int getPeriodAdjustedDay() {
        return periodAdjustedDay;
    }

    public ResolutionTypes getPeriodMode() {
        return periodMode;
    }

    public String getTimeZone() {
        return timeZone;
    }
    
    public void setBirth(String birth) {
        this.birth = birth;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public void setNotificationSettings(NotificationSettings notificationSettings) {
        this.notificationSettings = notificationSettings;
    }

    @JsonIgnore
    public void setNotificationSettingsSerialized(String notificationSettingsSerialized) {
        this.notificationSettings = SerializationUtils.deserializeFromString(notificationSettingsSerialized,
                NotificationSettings.class);
    }

    public void setPeriodAdjustedDay(int periodAdjustedDay) {
        this.periodAdjustedDay = periodAdjustedDay;
    }

    public void setPeriodMode(ResolutionTypes periodMode) {
        this.periodMode = periodMode;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getFraudPersonNumber() {
        return fraudPersonNumber;
    }

    public void setFraudPersonNumber(String fraudPersonNumber) {
        this.fraudPersonNumber = fraudPersonNumber;
    }

    public static class ProfileDateUtils {
        public static String getCurrentMonthPeriod(UserProfile profile) {
            return getMonthPeriod(DateUtils.getToday(), profile);
        }

        public static Date getLastDateFromPeriod(String period, UserProfile profile) {
            return DateUtils.getLastDateFromPeriod(period, profile.getPeriodMode(), profile.getPeriodAdjustedDay());
        }

        public static String getMonthPeriod(Date date, UserProfile profile) {
            return DateUtils.getMonthPeriod(date, profile.getPeriodMode(), profile.getPeriodAdjustedDay());
        }

        public static Date getFirstDateFromPeriod(String period, UserProfile profile) {
            return DateUtils.getFirstDateFromPeriod(period, profile.getPeriodMode(), profile.getPeriodAdjustedDay());
        }

        public static Period buildPeriod(String strPeriod, ResolutionTypes resolution, UserProfile userProfile) {
            switch (resolution) {
            case DAILY:
                return DateUtils.buildDailyPeriod(strPeriod);
            case WEEKLY:
                String locale = userProfile.getLocale();
                return DateUtils
                        .buildWeeklyPeriod(strPeriod, new Locale(locale.substring(0, 2), locale.substring(3, 5)));
            case MONTHLY:
            case MONTHLY_ADJUSTED:
                return DateUtils.buildMonthlyPeriod(strPeriod, resolution, userProfile.getPeriodAdjustedDay());
            case YEARLY:
                return DateUtils
                        .buildYearlyPeriod(Integer.parseInt(strPeriod.substring(0, 4)), userProfile.getPeriodMode(),
                                userProfile.getPeriodAdjustedDay());
            default:
                return DateUtils.buildDailyPeriod(strPeriod);
            }
        }
    }
}
