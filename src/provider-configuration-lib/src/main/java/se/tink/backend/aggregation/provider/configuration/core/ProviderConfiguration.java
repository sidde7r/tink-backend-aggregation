package se.tink.backend.aggregation.provider.configuration.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.Type;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.Field;
import se.tink.backend.core.ProviderRefreshSchedule;
import se.tink.backend.core.ProviderStatuses;
import se.tink.backend.core.ProviderTypes;
import se.tink.libraries.serialization.utils.SerializationUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;



// FIXME: remove all annotations and remove unused code
public class ProviderConfiguration {
    private static final String DEMO_AGENT_CLASS_NAME = "demo.DemoAgent";

    @SuppressWarnings("serial")
    private static class CapabilityList extends ArrayList<Capability> {}
    @SuppressWarnings("serial")
    private static class FieldsList extends ArrayList<Field> {}


    private String capabilitiesSerialized;
    private String className;
    private CredentialsTypes credentialsType;
    private String currency;
    private String displayName;
    private String fieldsSerialized;
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
    private String displayDescription;
    private String refreshScheduleSerialized;

    public ProviderConfiguration() {
        setFields(Lists.<Field> newArrayList());
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

    @ApiModelProperty(name = "status", value="The current status of the provider")
    public ProviderStatuses getStatus() {
        return status;
    }

    @ApiModelProperty(name = "type", value="The type of the provider")
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
        TRANSFERS, MORTGAGE_AGGREGATION
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


