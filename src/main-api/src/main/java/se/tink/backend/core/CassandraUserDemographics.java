/**
 * 
 */
package se.tink.backend.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

/**
 * Object to hold demographics information for user
 */

@Table(value = "users_demographics")
public class CassandraUserDemographics {
    private Integer age;
    private String birth;
    private String campaign;
    private String city;
    private String postalcode;
    private String community;
    private String country;
    private Date created;
    private Integer credentialsCount;
    private Long currentCategorization;
    private Date deleted;
    private Date firstUpdatedEvent;
    private Set<String> flags;
    private Integer followItemCount;
    private String gender;
    private Boolean hasFacebook;
    private Boolean hasPassword;
    private Long income;
    private Long initialCategorization;
    private Date lastUpdatedEvent;
    private String market;
    private Boolean organic;
    private Set<String> providers;
    private String source;
    private Integer taggedTransactionCount;
    private Integer transactionCount;
    private Integer uniqueTagCount;
    // Using UUID here because that's what most of the other tables in Cassandra are using. Makes JOINs easier.
    @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID userId;
    private Integer validCleanDataPeriodsCount;
    private Integer validCredentialsCount;
    private Double weeklyAuthErrorFrequency;
    private Double weeklyErrorFrequency;
    private Boolean hasHadTransactions;

    public Integer getAge() {
        return age;
    }
    
    public String getCampaign() {
        return campaign;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public Date getCreated() {
        return created;
    }

    public Integer getCredentialsCount() {
        return credentialsCount;
    }

    public Long getCurrentCategorization() {
        return currentCategorization;
    }

    public Date getDeleted() {
        return deleted;
    }

    public Date getFirstUpdatedEvent() {
        return firstUpdatedEvent;
    }

    public Set<String> getFlags() {
        return flags;
    }
    
    public Integer getFollowItemCount() {
        return followItemCount;
    }

    public String getGender() {
        return gender;
    }

    public Long getIncome() {
        return income;
    }

    public Long getInitialCategorization() {
        return initialCategorization;
    }

    public Date getLastUpdatedEvent() {
        return lastUpdatedEvent;
    }

    public String getMarket() {
        return market;
    }

    public Boolean getOrganic() {
        return organic;
    }

    public Set<String> getProviders() {
        return providers;
    }

    public String getSource() {
        return source;
    }

    public Integer getTaggedTransactionCount() {
        return taggedTransactionCount;
    }
    
    public Integer getTransactionCount() {
        return transactionCount;
    }
    
    public Integer getUniqueTagCount() {
        return uniqueTagCount;
    }

    public UUID getUserId() {
        return userId;
    }

    public Integer getValidCleanDataPeriodsCount() {
        return validCleanDataPeriodsCount;
    }

    public Integer getValidCredentialsCount() {
        return validCredentialsCount;
    }

    public Double getWeeklyAuthErrorFrequency() {
        return weeklyAuthErrorFrequency;
    }

    public Double getWeeklyErrorFrequency() {
        return weeklyErrorFrequency;
    }

    public boolean isHasFacebook() {
        return hasFacebook;
    }

    public boolean isHasPassword() {
        return hasPassword;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setCampaign(String campaign) {
        this.campaign = campaign;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setCredentialsCount(Integer credentialsCount) {
        this.credentialsCount = credentialsCount;
    }

    public void setCurrentCategorization(Long currentCategorization) {
        this.currentCategorization = currentCategorization;
    }

    public void setDeleted(Date deleted) {
        this.deleted = deleted;
    }

    public void setFirstUpdatedEvent(Date firstUpdatedEvent) {
        this.firstUpdatedEvent = firstUpdatedEvent;
    }

    public void setFlags(Set<String> flags) {
        this.flags = flags;
    }
    
    public void setFollowItemCount(Integer count) {
        this.followItemCount = count;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setHasFacebook(boolean hasFacebook) {
        this.hasFacebook = hasFacebook;
    }

    public void setHasPassword(boolean hasPassword) {
        this.hasPassword = hasPassword;
    }

    public void setIncome(Long income) {
        this.income = income;
    }

    public void setInitialCategorization(Long initialCategorization) {
        this.initialCategorization = initialCategorization;
    }

    public void setLastUpdatedEvent(Date lastUpdatedEvent) {
        this.lastUpdatedEvent = lastUpdatedEvent;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public void setOrganic(boolean organic) {
        this.organic = organic;
    }

    @JsonProperty
    public void setProviders(Set<String> providers) {
        this.providers = providers;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setTaggedTransactionCount(Integer count) {
        this.taggedTransactionCount = count;
    }
    
    public void setTransactionCount(Integer transactionCount) {
        this.transactionCount = transactionCount;
    }
    
    public void setUniqueTagCount(Integer count) {
        this.uniqueTagCount = count;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setValidCleanDataPeriodsCount(Integer validCleanDataPeriodsCount) {
        this.validCleanDataPeriodsCount = validCleanDataPeriodsCount;
    }

    public void setValidCredentialsCount(Integer validCredentialsCount) {
        this.validCredentialsCount = validCredentialsCount;
    }

    public void setWeeklyAuthErrorFrequency(Double weeklyAuthErrorFrequency) {
        this.weeklyAuthErrorFrequency = weeklyAuthErrorFrequency;
    }

    public void setWeeklyErrorFrequency(Double weeklyErrorFrequency) {
        this.weeklyErrorFrequency = weeklyErrorFrequency;
    }

    public void setHasHadTransactions(Boolean hasHadTransactions) {
        this.hasHadTransactions = hasHadTransactions;
    }

    public Boolean isHasHadTransactions() {
        return hasHadTransactions;
    }

    public String getPostalcode() {
        return postalcode;
    }

    public void setPostalcode(String postalcode) {
        this.postalcode = postalcode;
    }

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    public String getBirth() {
        return birth;
    }

    public void setBirth(String birth) {
        this.birth = birth;
    }
    
}
