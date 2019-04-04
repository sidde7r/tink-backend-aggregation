package se.tink.backend.aggregation.provider.configuration.storage.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import se.tink.libraries.credentials.enums.CredentialsTypes;
import se.tink.libraries.field.rpc.Field;
import se.tink.libraries.provider.enums.ProviderStatuses;
import se.tink.libraries.provider.enums.ProviderTypes;
import se.tink.libraries.provider.rpc.ProviderRefreshSchedule;
import se.tink.libraries.serialization.utils.SerializationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProviderConfiguration {

    public enum AccessType {
        OPEN_BANKING, OTHER
    }

    @SuppressWarnings("serial")
    private static class CapabilityList extends ArrayList<Capability> {}
    @SuppressWarnings("serial")
    private static class FieldsList extends ArrayList<Field> {}

    private AccessType accessType;
    private String capabilitiesSerialized;
    private String className;
    private CredentialsTypes credentialsType;
    private String currency;
    private String displayName;
    @JsonProperty("fields")
    private String fieldsSerialized;
    private String financialInstituteId;
    private String financialInstituteName;
    @JsonProperty("supplementalFields")
    private String supplementalFieldsSerialized;
    private String groupDisplayName;
    private String market;
    private boolean multiFactor;
    private String name;
    private String passwordHelpText;
    private String tutorialUrl;
    private String payload;
    private boolean popular;
    private double refreshFrequency = 1d;
    private double refreshFrequencyFactor = 1d;
    private ProviderStatuses status;
    private boolean transactional;
    private ProviderTypes type;
    private String displayDescription;
    private String refreshScheduleSerialized;

    public ProviderConfiguration() {
        setFields(Lists.<Field> newArrayList());
        setSupplementalFields(Lists.<Field> newArrayList());
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
        ProviderConfiguration other = (ProviderConfiguration) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    public AccessType getAccessType() {
        return accessType;
    }

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

    public String getFinancialInstituteId() {
        return financialInstituteId;
    }

    public String getFinancialInstituteName() {
        return financialInstituteName;
    }

    public String getTutorialUrl() {
        return tutorialUrl;
    }

    public List<Field> getSupplementalFields() {
        return SerializationUtils.deserializeFromString(supplementalFieldsSerialized, FieldsList.class);
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

    public String getPasswordHelpText() {
        return passwordHelpText;
    }

    public String getPayload() {
        return payload;
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

    public void setAccessType(
            AccessType accessType) {
        this.accessType = accessType;
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

    public void setFinancialInstituteId(String financialInstituteId) {
        this.financialInstituteId = financialInstituteId;
    }

    public void setFinancialInstituteName(String financialInstituteName) {
        this.financialInstituteName = financialInstituteName;
    }

    public void setSupplementalFields(List<Field> fields) {
        this.supplementalFieldsSerialized = SerializationUtils.serializeToString(fields);
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

    public void setTutorialUrl(String tutorialUrl) {
        this.tutorialUrl = tutorialUrl;
    }

    public void setType(ProviderTypes type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(ProviderConfiguration.class).add("name", name).toString();
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
    public Optional<ProviderRefreshSchedule> getRefreshSchedule() {
        if (Strings.isNullOrEmpty(refreshScheduleSerialized)) {
            return Optional.empty();
        }

        return Optional.ofNullable(
                SerializationUtils.deserializeFromString(refreshScheduleSerialized, ProviderRefreshSchedule.class));
    }
}


