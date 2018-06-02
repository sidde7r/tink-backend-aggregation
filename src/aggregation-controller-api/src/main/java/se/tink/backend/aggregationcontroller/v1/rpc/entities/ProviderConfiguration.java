package se.tink.backend.aggregationcontroller.v1.rpc.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
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
import org.hibernate.annotations.Type;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsTypes;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.ProviderStatuses;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.ProviderTypes;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Entity
@Table(name = "provider_configurations")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProviderConfiguration {
    @SuppressWarnings("serial")
    private static class CapabilityList extends ArrayList<ProviderConfiguration.Capability> {}
    @SuppressWarnings("serial")
    private static class FieldsList extends ArrayList<Field> {}

    @Column(name = "capabilities")
    private String capabilitiesSerialized;
    private String className;
    @Enumerated(EnumType.STRING)
    private CredentialsTypes credentialsType;
    private String currency;
    private String displayName;
    private String displayDescription;
    @JsonProperty("fields")
    @Column(name = "fields")
    @Type(type = "text")
    private String fieldsSerialized;
    private String groupDisplayName;
    private String market;
    private boolean multiFactor;
    @Id
    private String name;
    @Type(type = "text")
    private String passwordHelpText;
    private String payload;
    private boolean popular;
    private double refreshFrequency;
    private double refreshFrequencyFactor;
    @Enumerated(EnumType.STRING)
    private ProviderStatuses status;
    private boolean transactional;
    @Enumerated(EnumType.STRING)
    private ProviderTypes type;
    @Column(name = "refreshschedule")
    @Type(type = "text")
    private String refreshScheduleSerialized;

    @JsonProperty("capabilities")
    public Set<ProviderConfiguration.Capability> getCapabilities() {
        if (Strings.isNullOrEmpty(capabilitiesSerialized)) {
            return Sets.newHashSet();
        }

        return Sets.newConcurrentHashSet(
                SerializationUtils.deserializeFromString(capabilitiesSerialized, ProviderConfiguration.CapabilityList.class));
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
        return SerializationUtils.deserializeFromString(fieldsSerialized, ProviderConfiguration.FieldsList.class);
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
    public void setCapabilities(Set<ProviderConfiguration.Capability> capabilities) {
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

    /**
     * Used on providers to indicate different tasks it can handle in terms of agents, since it's not possible now in
     * main to know if an agent implements an interface e.g. TransferExecutor.
     */
    public enum Capability {
        TRANSFERS, MORTGAGE_AGGREGATION
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
