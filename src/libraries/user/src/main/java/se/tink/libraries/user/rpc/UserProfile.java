package se.tink.libraries.user.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;
import org.hibernate.annotations.Type;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.Period;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Embeddable
@SuppressWarnings("serial")
@Access(AccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserProfile implements Serializable {
    public static final Pattern PATTERN_BIRTH = Pattern.compile("([0-9]{4})(-([0-9]{2})(-([0-9]{2}))?)?");


    @Tag(1)
    @ApiModelProperty(name = "birth", hidden = true)
    private String birth; // Should be on form yyyy, yyyy-MM or yyyy-MM-dd
    // @Modifiable
    @Tag(2)
    private String currency;

    @Tag(3)
    @ApiModelProperty(name = "gender", hidden = true)
    private String gender;

    @Tag(4)
    private String locale;

    @Tag(5)
    private String market;

    @Transient
    @JsonProperty("notificationSettings")
    @Tag(6)
    private NotificationSettings notificationSettings;

    @Tag(7)
    private int periodAdjustedDay;

    @Enumerated(EnumType.STRING)
    @Tag(8)
    private ResolutionTypes periodMode;
    @Tag(9)
    private String timeZone;
    @Tag(10)
    @ApiModelProperty(name = "fraudPersonNumber", hidden = true)
    private String fraudPersonNumber;
    @Tag(11)
    @ApiModelProperty(name = "name", hidden = true)
    private String name;
    @Tag(12)
    private boolean cashbackEnabled = false;

    public String getBirth() {
        return birth;
    }

    @JsonIgnore
    @ApiModelProperty(name = "birthYear", hidden = true)
    public int getBirthYear() {
        if (birth != null) {
            Matcher m = PATTERN_BIRTH.matcher(birth);
            if (m.matches()) {
                return Integer.parseInt(m.group(1));
            }
        }

        return 0; // Jesus
    }

    @ApiModelProperty(name = "currency", value="The configured ISO 4217 currency code of the user. This can be modified by the user.", example="SEK", required = true)
    public String getCurrency() {
        return currency;
    }

    public String getGender() {
        return gender;
    }

    @ApiModelProperty(name = "locale", value="The configured locale of the user. This can be modified by the user.", example="sv_SE", required = true)
    public String getLocale() {
        return locale;
    }

    @ApiModelProperty(name = "market", value="The primary market/country of the user.", example="SE", required = true)
    public String getMarket() {
        return market;
    }

    public String getName() {
        return name;
    }

    @ApiModelProperty(name = "notificationSettings", value="The configured notification settings of the user. This can be modified by the user.", required = true)
    public NotificationSettings getNotificationSettings() {
        return notificationSettings;
    }

    @JsonIgnore
    @Access(AccessType.PROPERTY)
    @Column(name = "`notificationSettings`")
    @Type(type = "text")
    public String getNotificationSettingsSerialized() {
        return SerializationUtils.serializeToString(notificationSettings);
    }

    @ApiModelProperty(name = "periodAdjustedDay", value="The configured day of the month to break the adjusted period on. This can be modified by the user.", example="25", required = true)
    public int getPeriodAdjustedDay() {
        return periodAdjustedDay;
    }

    @ApiModelProperty(name = "periodMode", value="The configured monthly period mode of the user. This can be modified by the user.", example="MONTHLY_ADJUSTED", allowableValues = ResolutionTypes.PERIOD_MODE_DOCUMENTED, required = true)
    public ResolutionTypes getPeriodMode() {
        return periodMode;
    }

    @ApiModelProperty(name = "timeZone", value="The configured time zone of the user. This can be modified by the user.", example="Europe/Stockholm", required = true)
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
    @Access(AccessType.PROPERTY)
    @Column(name = "`notificationSettings`")
    @Type(type = "text")
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
