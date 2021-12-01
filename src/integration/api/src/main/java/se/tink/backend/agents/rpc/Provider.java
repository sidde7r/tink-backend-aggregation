package se.tink.backend.agents.rpc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.CaseFormat;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.FinancialService.FinancialServiceSegment;
import se.tink.libraries.provider.ProviderDto.ProviderTypes;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Provider implements Cloneable {

    public enum AccessType {
        OPEN_BANKING,
        OTHER
    }

    public enum AuthenticationFlow {
        EMBEDDED,
        REDIRECT,
        DECOUPLED
    }

    @Deprecated
    public enum AuthenticationUserType {
        PERSONAL,
        BUSINESS,
        CORPORATE
    }

    public enum AgentSource {
        AGGREGATION_SERVICE,
        STANDALONE_AGENT;
    }

    @SuppressWarnings("serial")
    private static class FieldsList extends ArrayList<Field> {}

    @JsonIgnore private static Logger logger = LoggerFactory.getLogger(Provider.class);

    private AccessType accessType;
    private AuthenticationFlow authenticationFlow;
    // Where in the aggregation structure the agent is placed - Format:
    // banks.se.alandsbanken.AlandsBankenAgent
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
    // Like the service provider SDC where there is a four character number that gives part of the
    // host URL.
    private String payload;
    private ProviderStatuses status;
    private ProviderTypes type;
    private String financialInstitutionId;
    @Deprecated private AuthenticationUserType authenticationUserType;
    private List<FinancialService> financialServices;
    private Set<AgentSource> agentSources;

    public Provider() {
        setFields(Lists.newArrayList());
        setSupplementalFields(Lists.newArrayList());
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public AuthenticationFlow getAuthenticationFlow() {
        return authenticationFlow;
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

    public String getFinancialInstitutionId() {
        return financialInstitutionId;
    }

    public void setAccessType(AccessType accessType) {
        this.accessType = accessType;
    }

    public void setAuthenticationFlow(AuthenticationFlow authenticationFlow) {
        this.authenticationFlow = authenticationFlow;
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

    public void setFinancialInstitutionId(String financialInstitutionId) {
        this.financialInstitutionId = financialInstitutionId;
    }

    @Deprecated
    public AuthenticationUserType getAuthenticationUserType() {
        return authenticationUserType;
    }

    @Deprecated
    public void setAuthenticationUserType(AuthenticationUserType authenticationUserType) {
        this.authenticationUserType = authenticationUserType;
    }

    public List<FinancialService> getFinancialServices() {
        return financialServices;
    }

    public void setFinancialServices(List<FinancialService> financialServices) {
        this.financialServices = financialServices;
    }

    @JsonIgnore
    public String getMetricTypeName() {
        return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_UNDERSCORE, getType().name());
    }

    @JsonIgnore
    public boolean isOpenBanking() {
        return accessType == AccessType.OPEN_BANKING;
    }

    @JsonIgnore
    public boolean hasSupportForBusinessSegment() {
        return hasSupportForSegmentType(FinancialServiceSegment.BUSINESS);
    }

    @JsonIgnore
    public boolean hasSupportForPersonalSegment() {
        return hasSupportForSegmentType(FinancialServiceSegment.PERSONAL);
    }

    @JsonIgnore
    private boolean hasSupportForSegmentType(FinancialServiceSegment segment) {
        return Optional.ofNullable(financialServices).orElse(Collections.emptyList()).stream()
                .map(FinancialService::getSegment)
                .anyMatch(s -> s == segment);
    }

    public Set<AgentSource> getAgentSources() {
        return agentSources;
    }

    public void setAgentSources(Set<AgentSource> agentSources) {
        this.agentSources = agentSources;
    }

    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof Provider)) {
            return false;
        }

        Provider objProvider = (Provider) obj;
        return objProvider.getName().equals(this.getName());
    }
}
