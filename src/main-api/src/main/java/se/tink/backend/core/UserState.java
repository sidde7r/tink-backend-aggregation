package se.tink.backend.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.Type;
import se.tink.backend.core.enums.RateThisAppStatus;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.date.Period;

@Entity
@Table(name = "users_states")
public class UserState implements Cloneable {
    private static final LogUtils log = new LogUtils(UserState.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int MAXIMUM_ALLOWED_PERIODS = 18;
    private static final TypeReference<List<se.tink.libraries.date.Period>> PERIODS_LIST_TYPE_REFERENCE = new TypeReference<List<Period>>() {
    };
    private static final TypeReference<List<String>> STRING_LIST_TYPE_REFERENCE = new TypeReference<List<String>>() {
    };

    private long activitiesTimestamp;
    private Long amountCategorizationLevel;
    private long contextTimestamp;
    private Long expensesLessThan10kCategorizationLevel;
    private boolean haveHadTransactions;
    private boolean haveManuallyFavoredAccount;
    private boolean haveSentWelcomeMessage;
    private Long initialAmountCategorizationLevel;
    private Long initialExpensesLessThan10kCategorizationLevel;
    private Long initialTransactionCategorizationLevel;
    @Column(name = "`periodBreakDates`")
    @Type(type = "text")
    private String periodBreakDatesSerialized;
    @Column(name = "`periods`")
    @Type(type = "text")
    private String periodsSerialized;
    private long statisticsTimestamp;
    @Column(name = "`tags`")
    @Type(type = "text")
    private String tagsSerialized;
    private Long transactionCategorizationLevel;
    @Id
    private String userId;
    private int userRefreshFrequency;
    @Column(name = "`validCleanDataPeriods`")
    @Type(type = "text")
    private String validCleanDataPeriodsSerialized;
    @Column(name = "`validPeriods`")
    @Type(type = "text")
    private String validPeriodsSerialized;
    @Transient
    private List<String> legacyPeriods;

    private Integer initialMerchantificationLevel;
    private Integer initialMerchantificationWithLocationLevel;
    private Integer merchantificationLevel;
    private Integer merchantificationWithLocationLevel;

    private Integer payday;
    private Date latestSalaryDate;
    private Date lastLogin;

    @Enumerated(EnumType.STRING)
    private RateThisAppStatus rateThisAppStatus = RateThisAppStatus.NOT_SENT;

    public UserState() {

    }

    public UserState(String userId) {
        this.userId = userId;
    }

    public long getActivitiesTimestamp() {
        return activitiesTimestamp;
    }

    public Long getAmountCategorizationLevel() {
        return amountCategorizationLevel;
    }

    public long getContextTimestamp() {
        return contextTimestamp;
    }

    public Long getExpensesLessThan10kCategorizationLevel() {
        return expensesLessThan10kCategorizationLevel;
    }

    public Long getInitialAmountCategorizationLevel() {
        return initialAmountCategorizationLevel;
    }

    public Long getInitialExpensesLessThan10kCategorizationLevel() {
        return initialExpensesLessThan10kCategorizationLevel;
    }

    public Long getInitialTransactionCategorizationLevel() {
        return initialTransactionCategorizationLevel;
    }

    public Date getLatestSalaryDate() {
        return latestSalaryDate;
    }

    public Integer getPayday() {
        return payday;
    }

    @JsonProperty("periods")
    public List<Period> getPeriods() {
        if (Strings.isNullOrEmpty(periodsSerialized)) {
            return Lists.newArrayList();
        }

        try {
            return MAPPER.readValue(periodsSerialized, PERIODS_LIST_TYPE_REFERENCE);
        } catch (Exception e) {
            log.error("Could not deserialize periods", e);
            return Lists.newArrayList();
        }
    }

    public long getStatisticsTimestamp() {
        return statisticsTimestamp;
    }

    @JsonProperty
    public List<String> getTags() {
        try {
            if (Strings.isNullOrEmpty(tagsSerialized)) {
                return Lists.newArrayList();
            } else {
                return MAPPER.readValue(tagsSerialized, STRING_LIST_TYPE_REFERENCE);
            }
        } catch (Exception e) {
            log.error("Could not deserialize tags", e);
            return Lists.newArrayList();
        }
    }

    public Long getTransactionCategorizationLevel() {
        return transactionCategorizationLevel;
    }

    public String getUserId() {
        return userId;
    }

    public int getUserRefreshFrequency() {
        return userRefreshFrequency;
    }

    @Deprecated
    @JsonProperty("validCleanDataPeriods")
    public List<String> getValidCleanDataPeriods() {
        return getValidPeriods();
    }

    @Deprecated
    @JsonProperty("validPeriods")
    public List<String> getValidPeriods() {

        if (legacyPeriods == null || legacyPeriods.isEmpty()) {
            List<Period> periods = getPeriods();

            if (periods.isEmpty()) {
                legacyPeriods = Lists.newArrayList();
            } else {
                legacyPeriods = Lists.reverse(Lists.newArrayList(Iterables.limit(
                        Lists.reverse(Lists.newArrayList(Iterables.transform(periods, Period::getName))),
                        MAXIMUM_ALLOWED_PERIODS)));
            }
        }

        return legacyPeriods;
    }

    public boolean isHaveHadTransactions() {
        return haveHadTransactions;
    }

    public boolean isHaveManuallyFavoredAccount() {
        return haveManuallyFavoredAccount;
    }

    public boolean isHaveSentWelcomeMessage() {
        return haveSentWelcomeMessage;
    }

    public void setActivitiesTimestamp(long activitiesTimestamp) {
        this.activitiesTimestamp = activitiesTimestamp;
    }

    public void setAmountCategorizationLevel(Long amountCategorizationLevel) {
        this.amountCategorizationLevel = amountCategorizationLevel;
    }

    public void setContextTimestamp(long contextTimestamp) {
        this.contextTimestamp = contextTimestamp;
    }

    public void setExpensesLessThan10kCategorizationLevel(Long expensesLessThan10kCategorizationLevel) {
        this.expensesLessThan10kCategorizationLevel = expensesLessThan10kCategorizationLevel;
    }

    public void setHaveHadTransactions(boolean haveHadTransactions) {
        this.haveHadTransactions = haveHadTransactions;
    }

    public void setHaveManuallyFavoredAccount(boolean haveManuallyFavoredAccount) {
        this.haveManuallyFavoredAccount = haveManuallyFavoredAccount;
    }

    public void setHaveSentWelcomeMessage(boolean haveSentWelcomeMessage) {
        this.haveSentWelcomeMessage = haveSentWelcomeMessage;
    }

    public void setInitialAmountCategorizationLevel(Long initialAmountCategorizationLevel) {
        this.initialAmountCategorizationLevel = initialAmountCategorizationLevel;
    }

    public void setInitialExpensesLessThan10kCategorizationLevel(Long initialExpensesLessThan10kCategorizationLevel) {
        this.initialExpensesLessThan10kCategorizationLevel = initialExpensesLessThan10kCategorizationLevel;
    }

    public void setInitialTransactionCategorizationLevel(Long initialTransactionCategorizationLevel) {
        this.initialTransactionCategorizationLevel = initialTransactionCategorizationLevel;
    }

    public void setLatestSalaryDate(Date latestSalaryDate) {
        this.latestSalaryDate = latestSalaryDate;
    }

    public void setPayday(Integer payday) {
        this.payday = payday;
    }

    @JsonProperty("periods")
    public void setPeriods(List<Period> periods) {

        // Gradually unset the deprecated period fields, when periods are recalculated/reset.
        validPeriodsSerialized = null;
        validCleanDataPeriodsSerialized = null;

        // Make the legacy periods to be regenerated next time they're requested
        legacyPeriods = null;

        try {
            periodsSerialized = MAPPER.writeValueAsString(periods);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void setStatisticsTimestamp(long statisticsTimestamp) {
        this.statisticsTimestamp = statisticsTimestamp;
    }

    @JsonProperty
    public void setTags(List<String> tags) {
        try {
            tagsSerialized = MAPPER.writeValueAsString(tags);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void setTransactionCategorizationLevel(Long transactionCategorizationLevel) {
        this.transactionCategorizationLevel = transactionCategorizationLevel;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserRefreshFrequency(int userRefreshFrequency) {
        this.userRefreshFrequency = userRefreshFrequency;
    }

    public Integer getInitialMerchantificationLevel() {
        return initialMerchantificationLevel;
    }

    public void setInitialMerchantificationLevel(Integer initialMerchantificationLevel) {
        this.initialMerchantificationLevel = initialMerchantificationLevel;
    }

    public Integer getInitialMerchantificationWithLocationLevel() {
        return initialMerchantificationWithLocationLevel;
    }

    public void setInitialMerchantificationWithLocationLevel(Integer initialMerchantificationWithLocationLevel) {
        this.initialMerchantificationWithLocationLevel = initialMerchantificationWithLocationLevel;
    }

    public Integer getMerchantificationLevel() {
        return merchantificationLevel;
    }

    public void setMerchantificationLevel(Integer merchantificationLevel) {
        this.merchantificationLevel = merchantificationLevel;
    }

    public Integer getMerchantificationWithLocationLevel() {
        return merchantificationWithLocationLevel;
    }

    public void setMerchantificationWithLocationLevel(Integer merchantificationWithLocationLevel) {
        this.merchantificationWithLocationLevel = merchantificationWithLocationLevel;
    }

    @Override
    public UserState clone() {
        try {
            return (UserState) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e); // Not really expected.
        }
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public RateThisAppStatus getRateThisAppStatus() {
        return rateThisAppStatus;
    }

    public void setRateThisAppStatus(RateThisAppStatus rateThisAppStatus) {
        this.rateThisAppStatus = rateThisAppStatus;
    }
}
