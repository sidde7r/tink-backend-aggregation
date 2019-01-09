package se.tink.backend.aggregation.rpc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.CaseFormat;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import se.tink.libraries.serialization.utils.SerializationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This Provider has been copied from {@link se.tink.backend.core.Provider} in an effort to remove :aggregation-apis
 * dependency on :main-api
 * <p>
 * Some of the objects here are not used by Aggregation at all, but are still needed until the Aggregation API has been
 * reworked. This is because users of the Aggregation API currently expects to get the same data back as it sends away
 * in the "enrichment" process.
 */
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Provider implements Cloneable {
    @SuppressWarnings("serial")
    private static class CapabilityList extends ArrayList<Capability> {
    }

    @SuppressWarnings("serial")
    private static class FieldsList extends ArrayList<Field> {
    }

    // Capabilities of the providers - Format: ["TRANSFERS","MORTGAGE_AGGREGATION"]
    private String capabilitiesSerialized;
    // Where in the aggregation structure the agent is placed - Format: banks.se.alandsbanken.AlandsBankenAgent
    private String className;
    private CredentialsTypes credentialsType;
    // What currency this provider is using for accounts and transactions. I.e. SEK, EUR.
    private String currency;
    // Name displayed in the app. I.e. Swedbank.
    private String displayName;
    @Deprecated
    private String email;

    @JsonProperty("fields")
    // See Field object (Aggregation).
    private String fieldsSerialized;

    // See Field object (Aggregation).
    private FieldsList supplementalFields;
    // In the list of all providers this is where the provider will be put.
    // I.e. All Swedbank agent is found under the group Swedbank.
    private String groupDisplayName;
    private String market;
    // True if this provider uses multifactor authentication - Mainly used on main for fraud stuff (ID koll)
    private boolean multiFactor;
    private String name;
    // Displayed in the view when adding a new credential.
    private String passwordHelpText;
    // Used if we need anything special for this provider.
    // Like the service provider SDC where there is a four character number that gives part of the host URL.
    private String payload;
    @Deprecated
    private String phone;
    // If true the provider will be displayed above the list of all providers.
    private boolean popular;

    @JsonIgnore
    // How many times per day the provider is automatically refreshed
    private double refreshFrequency;

    @JsonIgnore
    // 0 <= x <= 1 - The factor of the refreshFrequency (obviously...)
    private double refreshFrequencyFactor;

    private ProviderStatuses status;
    // True if the providers has transactions - I.e. False for CSN which only display the balance of the loan.
    private boolean transactional;
    private ProviderTypes type;
    @Deprecated
    private String url;
    // URL to tutorial how to add the specific credential
    private String tutorialUrl;
    private ImageUrls images;
    // Displayed under the provider name in the app when adding a new credential.
    private String displayDescription;

    public Provider() {
        setFields(Lists.newArrayList());
        setSupplementalFields(Lists.newArrayList());
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
    public String getAdditionalInformationCaption() {
        Field field = getField(Field.Key.ADDITIONAL_INFORMATION);

        return (field != null ? field.getDescription() : null);
    }

    @JsonProperty("capabilities")
    public Set<Capability> getCapabilities() {
        if (Strings.isNullOrEmpty(capabilitiesSerialized)) {
            return Sets.newHashSet();
        }

        return Sets.newConcurrentHashSet(
                SerializationUtils.deserializeFromString(capabilitiesSerialized, CapabilityList.class));
    }

    public String getCapabilitiesSerialized() {
        return capabilitiesSerialized;
    }

    public String getClassName() {
        return className;
    }

    public CredentialsTypes getCredentialsType() {
        return credentialsType;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDisplayDescription() {
        return displayDescription;
    }

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
        return supplementalFields;
    }

    public String getGroupDisplayName() {
        return groupDisplayName;
    }

    public String getMarket() {
        return market;
    }

    public String getName() {
        return name;
    }

    @Deprecated
    @JsonProperty
    public String getPasswordCaption() {
        Field field = getField(Field.Key.PASSWORD);

        return (field != null ? field.getDescription() : null);
    }

    public String getPasswordHelpText() {
        return passwordHelpText;
    }

    @Deprecated
    @JsonProperty
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
    public int getPasswordLength() {
        Field field = getField(Field.Key.PASSWORD);

        return ((field != null && field.getMaxLength() != null) ? field.getMaxLength() : 0);
    }

    public String getPayload() {
        return payload;
    }

    public String getPhone() {
        return phone;
    }

    public double getRefreshFrequency() {
        return refreshFrequency;
    }

    public double getRefreshFrequencyFactor() {
        return refreshFrequencyFactor;
    }

    public ProviderStatuses getStatus() {
        return status;
    }

    public ProviderTypes getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    @Deprecated
    @JsonProperty
    public String getUsernameCaption() {
        Field field = getField(Field.Key.USERNAME);

        return (field != null ? field.getDescription() : null);
    }

    @Deprecated
    @JsonProperty
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

    public boolean isMultiFactor() {
        return multiFactor;
    }

    public boolean isPopular() {
        return popular;
    }

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

    public void setFields(List<Field> fields) {
        this.fieldsSerialized = SerializationUtils.serializeToString(fields);
    }

    public void setSupplementalFields(List<Field> fields) {
        supplementalFields = new FieldsList();
        supplementalFields.addAll(fields);
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

    public ImageUrls getImages() {
        return images;
    }

    public void setImages(ImageUrls images) {
        this.images = images;
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

    @JsonIgnore
    public String getMetricTypeName() {
        return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_UNDERSCORE, getType().name());
    }
}
