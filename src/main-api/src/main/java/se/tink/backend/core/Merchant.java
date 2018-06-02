package se.tink.backend.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import io.protostuff.Exclude;
import io.protostuff.Tag;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.apache.commons.lang.ObjectUtils;
import org.hibernate.annotations.Type;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.utils.StringUtils;

@Entity
@Table(name = "merchants")
public class Merchant implements Serializable {

    private static final long serialVersionUID = 6718885122357993851L;

    @Exclude
    private String address;
    @Tag(1)
    private String categoryId;
    @Exclude
    private String city;
    @Embedded
    @Tag(2)
    private Coordinate coordinates;
    @Exclude
    private String country;
    @Tag(3)
    private String formattedAddress;
    @Id
    @Tag(4)
    private String id;
    @Creatable
    @Tag(5)
    private String name;
    @Exclude
    private String organizationId;
    @Exclude
    private String parentId;
    @Tag(6)
    private String phoneNumber;
    @Exclude
    private String photoAttributions;
    @Exclude
    private String photoReference;
    @Exclude
    private String postalCode;
    @Tag(7)
    private String reference;
    @Exclude
    private String SniCode;
    @Enumerated(EnumType.STRING)
    @Exclude
    private MerchantSources source;
    @Exclude
    @Transient
    @JsonIgnore
    private List<String> types;
    @Tag(8)
    private String website;
    @Exclude
    private Date created;
    @Tag(9)
    private boolean online;

    @Exclude
    @Column(name = "`visibletousers`")
    @Type(type = "text")
    private String visibleToUsersSerialized;

    @Override
    public boolean equals(Object obj) {
        Merchant m = (Merchant) obj;
        return (this.getId() != null && ObjectUtils.equals(this.getId(), m.getId()));
    }

    @PrePersist
    private void generateIdAndDateIfMissing() {
        if (id == null) {
            id = StringUtils.generateUUID();
        }

        if (created == null) {
            created = new Date();
        }
    }

    public String getAddress() {
        return address;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getCity() {
        return city;
    }

    public Coordinate getCoordinates() {
        return coordinates;
    }

    public String getCountry() {
        return country;
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public String getParentId() {
        return parentId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPhotoAttributions() {
        return photoAttributions;
    }

    public String getPhotoReference() {
        return photoReference;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getReference() {
        return reference;
    }

    public String getSniCode() {
        return SniCode;
    }

    public MerchantSources getSource() {
        return source;
    }

    public List<String> getTypes() {
        return types;
    }

    public String getWebsite() {
        return website;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCategoryId(String category) {
        this.categoryId = category;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCoordinates(Coordinate coordinates) {
        this.coordinates = coordinates;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setFormattedAddress(String formattedAdress) {
        this.formattedAddress = formattedAdress;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setPhotoAttributions(String photoAttributions) {
        this.photoAttributions = photoAttributions;
    }

    public void setPhotoReference(String photoReference) {
        this.photoReference = photoReference;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public void setSniCode(String sniCode) {
        SniCode = sniCode;
    }

    public void setSource(MerchantSources source) {
        this.source = source;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public boolean getOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    @JsonProperty("visibleToUsers")
    public String getVisibleToUsersSerialized() {
        return visibleToUsersSerialized;
    }

    public void setVisibleToUsersSerialized(String userIds) {
        visibleToUsersSerialized = userIds;
    }

    @JsonIgnore
    public List<String> getVisibleToUsers() {
        return SerializationUtils.deserializeFromString(visibleToUsersSerialized, new TypeReference<List<String>>() {
        });
    }

    public void setVisibleToUsers(List<String> userIds) {
        visibleToUsersSerialized = SerializationUtils.serializeToString(userIds);
    }

}
