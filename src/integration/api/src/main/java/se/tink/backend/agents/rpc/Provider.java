package se.tink.backend.agents.rpc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.CaseFormat;
import com.google.common.collect.Lists;
import se.tink.libraries.serialization.utils.SerializationUtils;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Provider implements Cloneable {
    @SuppressWarnings("serial")
    private static class FieldsList extends ArrayList<Field> {
    }

    // Where in the aggregation structure the agent is placed - Format: banks.se.alandsbanken.AlandsBankenAgent
    private String className;
    private CredentialsTypes credentialsType;
    // What currency this provider is using for accounts and transactions. I.e. SEK, EUR.
    private String currency;
    // Name displayed in the app. I.e. Swedbank.
    private String displayName;
    @JsonProperty("fields")
    // See Field object (Aggregation).
    private String fieldsSerialized;
    // See Field object (Aggregation).
    private FieldsList supplementalFields;
    private String market;
    private String name;
    // Used if we need anything special for this provider.
    // Like the service provider SDC where there is a four character number that gives part of the host URL.
    private String payload;
    private ProviderStatuses status;
    private ProviderTypes type;

    public Provider() {
        setFields(Lists.newArrayList());
        setSupplementalFields(Lists.newArrayList());
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

    public List<Field> getFields() {
        return SerializationUtils.deserializeFromString(fieldsSerialized, FieldsList.class);
    }

    public List<Field> getSupplementalFields() {
        return supplementalFields;
    }

    public String getMarket() {
        return market;
    }

    public String getName() {
        return name;
    }

    public String getPayload() {
        return payload;
    }

    public ProviderStatuses getStatus() {
        return status;
    }

    public ProviderTypes getType() {
        return type;
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

    public void setFields(List<Field> fields) {
        this.fieldsSerialized = SerializationUtils.serializeToString(fields);
    }

    public void setSupplementalFields(List<Field> fields) {
        supplementalFields = new FieldsList();
        supplementalFields.addAll(fields);
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public void setStatus(ProviderStatuses status) {
        this.status = status;
    }

    public void setType(ProviderTypes type) {
        this.type = type;
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
