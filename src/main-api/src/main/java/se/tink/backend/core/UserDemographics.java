/**
 * 
 */
package se.tink.backend.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Type;
import se.tink.backend.utils.LogUtils;

/**
 * Object to hold demographics information for user
 */

@Entity
@Table(name = "users_demographics")
public class UserDemographics {
    private static LogUtils log = new LogUtils(UserDemographics.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<List<String>> STRING_LIST_TYPE_REFERENCE = new TypeReference<List<String>>() {
    };
    private static final TypeReference<Set<String>> STRING_SET_TYPE_REFERENCE = new TypeReference<Set<String>>() {
    };

    private Integer age;
    private String birth;
    private String campaign;
    private String city;
    private String country;
    private String community;
    private String postalCode;
    private Date created;
    private Integer credentialsCount;
    private Long currentCategorization;
    private Date deleted;
    private Date firstUpdatedEvent;
    @Column(name = "`flags`")
    @Type(type = "text")
    private String flagsSerialized;
    private Integer followItemCount;
    private String gender;
    private boolean hasFacebook;
    private Boolean hasHadTransactions;
    private boolean hasPassword;
    private Long income;
    private Long initialCategorization;
    private Date lastUpdatedEvent;
    private String market;
    private boolean organic;
    @Column(name = "`providers`")
    @Type(type = "text")
    private String providersSerialized;
    private String source;
    private Integer taggedTransactionCount;
    private Integer transactionCount;
    private Integer uniqueTagCount;
    @Id
    private String userId;
    private Integer validCleanDataPeriodsCount;
    private Integer validCredentialsCount;
    private Double weeklyAuthErrorFrequency;
    private Double weeklyErrorFrequency;

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

    @JsonProperty
    public Set<String> getFlags() {
        try {
            if (Strings.isNullOrEmpty(flagsSerialized)) {
                return Sets.newHashSet();
            } else {
                return MAPPER.readValue(flagsSerialized, STRING_SET_TYPE_REFERENCE);
            }
        } catch (Exception e) {
            log.error("Could not deserialize flags", e);
            return Sets.newHashSet();
        }
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

    @JsonProperty
    public List<String> getProviders() {
        try {
            if (Strings.isNullOrEmpty(providersSerialized)) {
                return Lists.newArrayList();
            } else {
                return MAPPER.readValue(providersSerialized, STRING_LIST_TYPE_REFERENCE);
            }
        } catch (Exception e) {
            log.error("Could not deserialize providers", e);
            return Lists.newArrayList();
        }
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

    public String getUserId() {
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
    
    public boolean isOrganic() {
        return organic;
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

    @JsonProperty
    public void setFlags(Set<String> flags) {
        try {
            flagsSerialized = MAPPER.writeValueAsString(flags);
        } catch (JsonProcessingException e) {
            log.error("Could not serialize flags", e);
        }
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
    public void setProviders(List<String> providers) {
        try {
            providersSerialized = MAPPER.writeValueAsString(providers);
        } catch (JsonProcessingException e) {
            log.error("Could not serialize providers", e);
        }
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

    public void setUserId(String userId) {
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

    public Map<String, Object> toProperties() {
        Map<String, Object> properties = Maps.newHashMap();

        if (age != null) {
            properties.put("Age", age);
        }

        if (gender != null) {
            properties.put("Gender", gender);
        }

        if (transactionCount != null) {
            properties.put("Number of transactions", transactionCount);
        }

        if (credentialsCount != null) {
            properties.put("Number of credentials", credentialsCount);
        }

        if (validCredentialsCount != null) {
            properties.put("Number of credentials (valid)", validCredentialsCount);
        }

        if (validCredentialsCount != null && credentialsCount != null) {
            properties.put("Number of credentials (invalid)", credentialsCount - validCredentialsCount);
        }

        if (weeklyErrorFrequency != null) {
            properties.put("Weekly error frequency", weeklyErrorFrequency);
        }

        if (lastUpdatedEvent != null) {
            properties.put("Last updated", lastUpdatedEvent);
        }

        if (firstUpdatedEvent != null) {
            properties.put("First updated", firstUpdatedEvent);
        }

        if (validCleanDataPeriodsCount != null) {
            properties.put("Number of periods", validCleanDataPeriodsCount);
        }

        if (city != null) {
            properties.put("City (internal)", city);
        }

        if (country != null) {
            properties.put("Country (internal)", country);
        }

        if (followItemCount != null) {
            properties.put("Number of budgets", followItemCount);
        }

        if (taggedTransactionCount != null) {
            properties.put("Number of tagged transactions", taggedTransactionCount);
        }

        if (uniqueTagCount != null) {
            properties.put("Number of unique tags", uniqueTagCount);
        }
        
        return properties;
    }

    public Boolean isHasHadTransactions() {
        return hasHadTransactions;
    }

    public void setHasHadTransactions(boolean hasHadTransactions) {
        this.hasHadTransactions = hasHadTransactions;
    }

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getBirth() {
        return birth;
    }

    public void setBirth(String birth) {
        this.birth = birth;
    }
}
