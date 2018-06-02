package se.tink.backend.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Iterator;
import se.tink.libraries.date.ResolutionTypes;

@SuppressWarnings("serial")
@JsonInclude(Include.NON_NULL)
public class Statistic implements Serializable {
    public static class Types {
        public static final String BALANCES_BY_ACCOUNT = "balances-by-account";
        public static final String BALANCES_BY_ACCOUNT_TYPE_GROUP = "balances-by-account-type-group";

        public static final String EXPENSES_BY_CATEGORY = "expenses-by-category";
        public static final String EXPENSES_COUNT_BY_CATEGORY = "expenses-by-category/by-count";

        public static final String INCOME_AND_EXPENSES = "income-and-expenses";
        public static final String INCOME_AND_EXPENSES_AND_TRANSFERS = "income-and-expenses-and-transfers";
        public static final String INCOME_AND_EXPENSES_COUNT = "income-and-expenses/by-count";
        public static final String INCOME_BY_CATEGORY = "income-by-category";
        public static final String INCOME_NET = "net-income";

        public static final String LEFT_TO_SPEND = "left-to-spend";
        public static final String LEFT_TO_SPEND_AVERAGE = "left-to-spend-average";

        public static final String LOAN_RATES_BY_PROPERTY = "loan-rates-by-property";
        public static final String LOAN_BALANCES_BY_PROPERTY = "loan-balances-by-property";
    }

    public static Statistic copyOf(Statistic o) {
        Statistic s = new Statistic();

        s.setDescription(o.getDescription());
        s.setPeriod(o.getPeriod());
        s.setResolution(o.getResolution());
        s.setType(o.getType());
        s.setUserId(o.getUserId());
        s.setValue(o.getValue());
        s.setPayload(o.getPayload());

        return s;
    }

    @Tag(1)
    @ApiModelProperty(name = "description", value = "Identifier of the data the statistic represents.", example = "fe9e199c2ca94c12baf1f3eb4a4122de", required = true)
    private String description;
    @Tag(2)
    @ApiModelProperty(name = "payload", value = "Secondary identifier of the data the statistic represent", example = "690667930d7e4f2ba0d9aa5f7d2a1941")
    private String payload;
    @Tag(3)
    @ApiModelProperty(name = "period", value = "The statistic's period, depends on it's resolution. On of: year, month, week or day. Format: '2014', '2014-02', 2014:45 or '2014-02-12'", example = "2014-12-15", required = true)
    private String period;
    @Tag(4)
    @ApiModelProperty(name = "resolution", value = "Resolution for the statistics.", example = "DAILY", required = true)
    private ResolutionTypes resolution;
    @Tag(5)
    @ApiModelProperty(name = "type", value = "The statistic's type.", example = "expenses-by-category", required = true)
    private String type;
    @Tag(6)
    @ApiModelProperty(name = "userId", value = "The internal identifier of the user that the statistics belongs to.", example = "d9f134ee2eb44846a4e02990ecc8d32e", required = true)
    private String userId;
    @Tag(7)
    @ApiModelProperty(name = "value", value = "The value of the statistics for this type, period, and description.", example = "1298.5", required = true)
    private double value;

    public Statistic() {

    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Statistic) {
            final Statistic other = (Statistic) obj;

            return (Objects.equal(userId, other.userId) && Objects.equal(period, other.period)
                    && Objects.equal(resolution, other.resolution) && Objects.equal(type, other.type) && Objects.equal(
                    description, other.description));
        } else {
            return false;
        }
    }

    public String getDescription() {
        return description;
    }

    public String getPayload() {
        return payload;
    }

    @JsonIgnore
    public Integer getTruncatedPeriod() {
        return getYearMonth();
    }

    /**
     * A utility method to truncate the statistic period to a monthly
     * period of the format yyyyMM for DAILY, MONTHLY AND WEEKLY
     * for YEARLY it uses yyyy00
     *
     * @return the monthly period
     */
    private Integer getYearMonth() {
        switch (resolution) {
        case DAILY:
            Iterator<String> dailyPeriod = Splitter.on('-').split(period).iterator();
            return Integer.parseInt(dailyPeriod.next()+dailyPeriod.next());
        case WEEKLY:
            Iterator<String> weeklyPeriod= Splitter.on(':').split(period).iterator();
            String yearString = weeklyPeriod.next();
            int year = Integer.parseInt(yearString);
            Integer week = Integer.parseInt(weeklyPeriod.next());
            LocalDate dateTime = LocalDate.ofYearDay(year, 1).withYear(year).plusWeeks(week);
            String month = String.format("%02d", dateTime.getMonth().getValue());
            return Integer.parseInt(yearString + month);
        case YEARLY:
            return Integer.parseInt(period + "00");
        default:
            return Integer.parseInt(period.replace("-", ""));
        }
    }

    public String getPeriod() {
        return period;
    }

    public ResolutionTypes getResolution() {
        return resolution;
    }

    public String getType() {
        return type;
    }

    public String getUserId() {
        return userId;
    }

    public double getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userId, period, resolution, type, description);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public void setResolution(ResolutionTypes resolution) {
        this.resolution = resolution;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("userId", userId).add("period", period)
                .add("resolution", resolution)
                .add("type", type).add("description", description).add("value", value).add("payload", payload)
                .toString();
    }

}
