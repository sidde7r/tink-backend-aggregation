package se.tink.backend.core.property;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import io.protostuff.Exclude;
import io.protostuff.Tag;
import java.util.Date;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.Type;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.core.Modifiable;
import se.tink.backend.serialization.TypeReferences;

@Entity
@Table(name = "properties")
public class Property {
    @Id
    @Tag(1)
    private String id;
    @Exclude
    @JsonIgnore
    private String userId;
    @Tag(2)
    @Enumerated(EnumType.STRING)
    private PropertyType type;
    @Tag(3)
    private String address;
    @Tag(4)
    private String postalCode;
    @Tag(5)
    private String city;
    @Tag(6)
    private String community;
    @Tag(7)
    @Modifiable
    private Integer numberOfRooms;
    @Tag(8)
    @Modifiable
    private Integer numberOfSquareMeters;
    @Transient
    @Tag(9)
    private Set<String> loanAccountIds;
    @Tag(10)
    private boolean userModifiedLoanAccountIds;
    @Tag(11)
    private boolean registeredAddress;
    @Tag(12)
    private Integer mostRecentValuation;
    @Tag(13)
    private Double latitude;
    @Tag(14)
    private Double longitude;
    @Exclude
    @JsonIgnore
    @Enumerated(EnumType.STRING)
    private PropertyStatus status;
    @Exclude
    @JsonIgnore
    @Column(name = "`loanAccountIds`")
    @Type(type = "text")
    private String loanAccountIdsSerialized;
    @Exclude
    @JsonIgnore
    private Date created;
    @Exclude
    @JsonIgnore
    private String booliEstimateId;

    public String getLoanAccountIdsSerialized() {
        return loanAccountIdsSerialized;
    }

    public void setLoanAccountIdsSerialized(String loanAccountIdsSerialized) {
        if (Strings.isNullOrEmpty(loanAccountIdsSerialized)) {
            setLoanAccountIds(null);
        } else {
            setLoanAccountIds(
                    SerializationUtils.deserializeFromString(loanAccountIdsSerialized, TypeReferences.SET_OF_STRINGS));
        }
    }

    public PropertyStatus getStatus() {
        return status;
    }

    public void setStatus(PropertyStatus status) {
        this.status = status;
    }

    public boolean isUserModifiedLoanAccountIds() {
        return userModifiedLoanAccountIds;
    }

    public void setUserModifiedLoanAccountIds(boolean userModifiedLoanAccountIds) {
        this.userModifiedLoanAccountIds = userModifiedLoanAccountIds;
    }

    public boolean isRegisteredAddress() {
        return registeredAddress;
    }

    public void setRegisteredAddress(boolean registeredAddress) {
        this.registeredAddress = registeredAddress;
    }

    public Set<String> getLoanAccountIds() {
        if (loanAccountIds == null && !Strings.isNullOrEmpty(loanAccountIdsSerialized)) {
            this.loanAccountIds = SerializationUtils
                    .deserializeFromString(loanAccountIdsSerialized, TypeReferences.SET_OF_STRINGS);
        }

        return loanAccountIds != null ? loanAccountIds : Sets.newHashSet();
    }

    public void setLoanAccountIds(Set<String> loanAccountIds) {
        this.loanAccountIds = loanAccountIds;
        this.loanAccountIdsSerialized = SerializationUtils.serializeToString(loanAccountIds);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PropertyType getType() {
        return type;
    }

    public void setType(PropertyType type) {
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    public Integer getNumberOfRooms() {
        return numberOfRooms;
    }

    public void setNumberOfRooms(Integer numberOfRooms) {
        this.numberOfRooms = numberOfRooms;
    }

    public Integer getNumberOfSquareMeters() {
        return numberOfSquareMeters;
    }

    public void setNumberOfSquareMeters(Integer numberOfSquareMeters) {
        this.numberOfSquareMeters = numberOfSquareMeters;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getMostRecentValuation() {
        return mostRecentValuation;
    }

    public void setMostRecentValuation(Integer mostRecentValuation) {
        this.mostRecentValuation = mostRecentValuation;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setBooliEstimateId(String booliEstimateId) {
        this.booliEstimateId = booliEstimateId;
    }

    public String getBooliEstimateId() {
        return booliEstimateId;
    }
}
