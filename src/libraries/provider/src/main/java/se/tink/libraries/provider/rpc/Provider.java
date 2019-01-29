package se.tink.libraries.provider.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.protostuff.Exclude;
import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.Type;
import se.tink.backend.core.Field;
import se.tink.libraries.credentials.enums.CredentialsTypes;
import se.tink.libraries.provider.enums.ProviderStatuses;
import se.tink.libraries.provider.enums.ProviderTypes;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Entity
@Table(name = "providers")
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Provider implements Cloneable {
    private static final String DEMO_AGENT_CLASS_NAME = "demo.DemoAgent";

    @SuppressWarnings("serial")
    private static class CapabilityList extends ArrayList<Capability> {}
    @SuppressWarnings("serial")
    private static class FieldsList extends ArrayList<Field> {}

    @Tag(15)
    @Column(name = "`capabilities`")
    @Type(type = "text")
    @ApiModelProperty(name = "capabilitiesSerialized", hidden = true)
    private String capabilitiesSerialized;
    @Exclude
    @ApiModelProperty(name = "className", hidden = true)
    private String className;
    @Enumerated(EnumType.STRING)
    @Tag(5)
    private CredentialsTypes credentialsType;
    @Exclude
    private String currency;
    @Tag(2)
    private String displayName;
    @Exclude
    private String email;
    @JsonProperty("fields")
    @Column(name = "`fields`")
    @Type(type = "text")
    @Tag(9)
    private String fieldsSerialized;
    @JsonProperty("supplementalFields")
    @Transient
    @Tag(16)
    private String supplementalFieldsSerialized;
    @Tag(10)
    private String groupDisplayName;
    @Exclude
    private String market;
    @Exclude
    private boolean multiFactor;
    @Id
    @Tag(1)
    private String name;
    @Type(type = "text")
    @Tag(6)
    private String passwordHelpText;
    @Type(type = "text")
    @Exclude
    @ApiModelProperty(name = "payload", hidden = true)
    private String payload;
    @Exclude
    private String phone;
    @Tag(7)
    private boolean popular;
    @JsonIgnore
    @Exclude
    @ApiModelProperty(name = "refreshFrequency", hidden = true)
    private double refreshFrequency;
    @JsonIgnore
    @Exclude
    private double refreshFrequencyFactor;
    @Enumerated(EnumType.STRING)
    @Tag(4)
    private ProviderStatuses status;
    @Tag(8)
    private boolean transactional;
    @Enumerated(EnumType.STRING)
    @Tag(3)
    private ProviderTypes type;
    @Exclude
    private String url;
    @Tag(11)
    @ApiModelProperty(name = "tutorialUrl", hidden = true)
    private String tutorialUrl;
    @Tag(14)
    private String displayDescription;
    @Column(name = "`refreshschedule`")
    @Type(type = "text")
    @Exclude
    private String refreshScheduleSerialized;

    public Provider() {
        setFields(Lists.<Field> newArrayList());
        setSupplementalFields(Lists.<Field> newArrayList());
    }

    @Override
    public Provider clone() {
        try {
            return (Provider) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    @Override
    public boolean equals(Object obj) {
        // Generated using Eclipse
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Provider other = (Provider) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Deprecated
    @JsonProperty
    @ApiModelProperty(name = "additionalInformationCaption", hidden = true)
    public String getAdditionalInformationCaption() {
        Field field = getField(Field.Key.ADDITIONAL_INFORMATION);

        return (field != null ? field.getDescription() : null);
    }

    @ApiModelProperty(name = "capabilities", hidden = true)
    @JsonProperty("capabilities")
    public Set<Capability> getCapabilities() {
        if (Strings.isNullOrEmpty(capabilitiesSerialized)) {
            return Sets.newHashSet();
        }

        return Sets.newConcurrentHashSet(SerializationUtils.deserializeFromString(capabilitiesSerialized, CapabilityList.class));
    }

    public String getCapabilitiesSerialized() {
        return capabilitiesSerialized;
    }

    public String getClassName() {
        return className;
    }

    @ApiModelProperty(name = "credentialsType", value="The type of credentials the provider creates", example = "MOBILE_BANKID", allowableValues = CredentialsTypes.DOCUMENTED)
    public CredentialsTypes getCredentialsType() {
        return credentialsType;
    }

    @ApiModelProperty(name = "currency", value="The default currency of the provider", example = "SEK")
    public String getCurrency() {
        return currency;
    }

    @ApiModelProperty(name = "displayName", value="The display name of the provider", example = "Handelsbanken")
    public String getDisplayName() {
        return displayName;
    }

    @ApiModelProperty(name = "displayDescription", value="The display description of the provider", example = "Mobilt BankID")
    public String getDisplayDescription() {
        return displayDescription;
    }

    @ApiModelProperty(name = "email", value="The contact information email to the provider")
    public String getEmail() {
        return email;
    }

    private Field getField(final String name) {
        List<Field> fields = getFields();

        if (fields == null || fields.size() == 0) {
            return null;
        }

        Field field = fields.stream().filter(f -> Objects.equal(f.getName(), name)).findFirst().orElse(null);

        return field;
    }

    private Field getField(final Field.Key key) {
        return getField(key.getFieldKey());
    }

    public List<Field> getFields() {
        return SerializationUtils.deserializeFromString(fieldsSerialized, FieldsList.class);
    }

    public List<Field> getSupplementalFields() {
        return SerializationUtils.deserializeFromString(supplementalFieldsSerialized, FieldsList.class);
    }

    @ApiModelProperty(name = "groupDisplayName", value="The grouped display name of the provider")
    public String getGroupDisplayName() {
        return groupDisplayName;
    }

    @ApiModelProperty(name = "market", value="The market of the provider")
    public String getMarket() {
        return market;
    }

    @ApiModelProperty(name = "name", value="The short name of the provider")
    public String getName() {
        return name;
    }

    @Deprecated
    @JsonProperty
    @ApiModelProperty(name = "passwordCaption", hidden = true)
    public String getPasswordCaption() {
        Field field = getField(Field.Key.PASSWORD);

        return (field != null ? field.getDescription() : null);
    }

    public String getPasswordHelpText() {
        return passwordHelpText;
    }

    @Deprecated
    @JsonProperty
    @ApiModelProperty(name = "passwordIsPIN", hidden = true)
    public boolean getPasswordIsPIN() {
        Field field = getField(Field.Key.PASSWORD);

        if (field == null) {
            return false;
        }

        if (Objects.equal("PIN-kod", field.getDescription())) {
            return true;
        }

        return false;
    }

    @Deprecated
    @JsonProperty
    @ApiModelProperty(name = "passwordLength", hidden = true)
    public int getPasswordLength() {
        Field field = getField(Field.Key.PASSWORD);

        return ((field != null && field.getMaxLength() != null) ? field.getMaxLength() : 0);
    }

    public String getPayload() {
        return payload;
    }

    @ApiModelProperty(name = "phone", value="The contact information phone number to the provider")
    public String getPhone() {
        return phone;
    }

    public double getRefreshFrequency() {
        return refreshFrequency;
    }

    public double getRefreshFrequencyFactor() {
        return refreshFrequencyFactor;
    }

    @ApiModelProperty(name = "status", value="The current status of the provider")
    public ProviderStatuses getStatus() {
        return status;
    }

    @ApiModelProperty(name = "type", value="The type of the provider")
    public ProviderTypes getType() {
        return type;
    }

    @ApiModelProperty(name = "url", value="The contact information URL to the provider")
    public String getUrl() {
        return url;
    }

    @Deprecated
    @JsonProperty
    @ApiModelProperty(name = "usernameCaption", hidden = true)
    public String getUsernameCaption() {
        Field field = getField(Field.Key.USERNAME);

        return (field != null ? field.getDescription() : null);
    }

    @Deprecated
    @JsonProperty
    @ApiModelProperty(name = "usernameIsPersonnummer", hidden = true)
    public boolean getUsernameIsPersonnummer() {
        Field field = getField(Field.Key.USERNAME);

        if (field == null) {
            return false;
        }

        if (Objects.equal(field.getHint(), "ÅÅÅÅMMDDNNNN") || Objects.equal(field.getHint(), "ÅÅMMDDNNNN")) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        // Generated using Eclipse
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @ApiModelProperty(name = "multiFactor", value="Flag to indicate if the provider requires multi-factor authentication")
    public boolean isMultiFactor() {
        return multiFactor;
    }

    @ApiModelProperty(name = "popular", value="Flag to indicate if the provider is popular")
    public boolean isPopular() {
        return popular;
    }

    @ApiModelProperty(name = "transactional", value="Flag to indicate if the provider provides transactional data")
    public boolean isTransactional() {
        return transactional;
    }

    @JsonProperty("capabilities")
    public void setCapabilities(Set<Capability> capabilities) {
        if (capabilities == null) {
            this.capabilitiesSerialized = null;
        }

        this.capabilitiesSerialized = SerializationUtils.serializeToString(capabilities);
    }

    public void setCapabilitiesSerialized(String capabilitiesSerialized) {
        this.capabilitiesSerialized = capabilitiesSerialized;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setCredentialsType(CredentialsTypes credentialsType) {
        this.credentialsType = credentialsType;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setDisplayDescription(String displayDescription) {
        this.displayDescription = displayDescription;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFields(List<Field> fields) {
        this.fieldsSerialized = SerializationUtils.serializeToString(fields);
    }

    public void setSupplementalFields(List<Field> supplementalFields) {
        this.supplementalFieldsSerialized = SerializationUtils.serializeToString(supplementalFields);
    }

    public void setGroupDisplayName(String groupDisplayName) {
        this.groupDisplayName = groupDisplayName;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public void setMultiFactor(boolean multiFactor) {
        this.multiFactor = multiFactor;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPasswordHelpText(String passwordHelpText) {
        this.passwordHelpText = passwordHelpText;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setPopular(boolean popular) {
        this.popular = popular;
    }

    public void setRefreshFrequency(double refreshFrequency) {
        this.refreshFrequency = refreshFrequency;
    }

    public void setRefreshFrequencyFactor(double refreshFrequencyFactor) {
        this.refreshFrequencyFactor = refreshFrequencyFactor;
    }

    public void setStatus(ProviderStatuses status) {
        this.status = status;
    }

    public void setTransactional(boolean transactional) {
        this.transactional = transactional;
    }

    public void setType(ProviderTypes type) {
        this.type = type;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(Provider.class).add("name", name).toString();
    }

    public String getTutorialUrl() {
        return tutorialUrl;
    }

    public void setTutorialUrl(String tutorialUrl) {
        this.tutorialUrl = tutorialUrl;
    }

    @JsonIgnore
    @Transient
    public double getCurrentRefreshFrequency() {
        return refreshFrequency * refreshFrequencyFactor;
    }

    @JsonIgnore
    @Transient
    public String getCleanDisplayName() {
        // Some of our display names have the authetication method in a parenthesis after.
        // e.g. Handelsbanken (Mobilt BankID)
        return displayName.replaceAll(" \\([\\w \\-]+\\)", "");
    }

    @JsonIgnore
    @Transient
    public boolean isUsingDemoAgent() {
        return DEMO_AGENT_CLASS_NAME.equals(getClassName());
    }

    /**
     * Used on providers to indicate different tasks it can handle in terms of agents, since it's not possible now in
     * main to know if an agent implements an interface e.g. TransferExecutor.
     */
    public enum Capability {
        TRANSFERS,              // backwards compatibility
        MORTGAGE_AGGREGATION,   // backwards compatibility
        CHECKING_ACCOUNTS,
        SAVINGS_ACCOUNTS,
        CREDIT_CARDS,
        LOANS,
        INVESTMENTS,
        PAYMENTS
    }

    @JsonProperty("refreshschedule")
    public void setRefreshSchedule(ProviderRefreshSchedule refreshSchedule) {
        this.refreshScheduleSerialized = SerializationUtils.serializeToString(refreshSchedule);
    }

    @JsonIgnore
    @Transient
    public Optional<ProviderRefreshSchedule> getRefreshSchedule() {
        if (Strings.isNullOrEmpty(refreshScheduleSerialized)) {
            return Optional.empty();
        }

        return Optional.ofNullable(
                SerializationUtils.deserializeFromString(refreshScheduleSerialized, ProviderRefreshSchedule.class));
    }
}
