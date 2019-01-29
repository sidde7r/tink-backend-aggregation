package se.tink.backend.aggregation.provider.configuration.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import se.tink.libraries.credentials.enums.CredentialsTypes;
import se.tink.backend.core.Field;
import se.tink.libraries.provider.rpc.ProviderRefreshSchedule;
import se.tink.libraries.provider.enums.ProviderStatuses;
import se.tink.libraries.provider.enums.ProviderTypes;
import se.tink.libraries.serialization.utils.SerializationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;


// FIXME: remove all code that has JSON IGNORE
public class ProviderConfigurationDTO {
    @SuppressWarnings("serial")
    private static class CapabilityList extends ArrayList<Capability> {
    }

    @SuppressWarnings("serial")
    private static class FieldsList extends ArrayList<Field> {
    }

    private String capabilitiesSerialized;
    private String className;
    private CredentialsTypes credentialsType;
    private String currency;
    private String displayName;
    private String displayDescription;
    @JsonProperty("fields")
    private String fieldsSerialized;
    @JsonProperty("supplementalFields")
    private String supplementalFieldsSerialized;
    private String groupDisplayName;
    private String market;
    private boolean multiFactor;
    private String name;
    private String passwordHelpText;
    private String payload;
    private boolean popular;
    private double refreshFrequency;
    private double refreshFrequencyFactor;
    private ProviderStatuses status;
    private boolean transactional;
    private ProviderTypes type;
    private String refreshScheduleSerialized;

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

    private Field getField(final String name) {
        List<Field> fields = getFields();

        if (fields == null || fields.isEmpty()) {
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

    public List<Field> getSupplementalFields() {
        return SerializationUtils.deserializeFromString(supplementalFieldsSerialized, FieldsList.class);
    }

    public ProviderTypes getType() {
        return type;
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

    public void setSupplementalFields(List<Field> supplementalFields) {
        this.supplementalFieldsSerialized = SerializationUtils.serializeToString(supplementalFields);
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
