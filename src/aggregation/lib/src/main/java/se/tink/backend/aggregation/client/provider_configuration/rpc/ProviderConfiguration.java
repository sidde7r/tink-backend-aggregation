package se.tink.backend.aggregation.client.provider_configuration.rpc;

import static io.vavr.Predicates.not;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import se.tink.libraries.provider.ProviderDto.ProviderTypes;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProviderConfiguration {

    public enum AccessType {
        OPEN_BANKING,
        OTHER
    }

    public enum AuthenticationFlow {
        EMBEDDED,
        REDIRECT,
        DECOUPLED
    }

    public enum ReleaseStatus {
        BETA,
        LIMITED_RELEASE,
        GENERALLY_AVAILABLE
    }

    public enum ComplianceStatus {
        COMPLIANT,
        NON_COMPLIANT
    }

    @Deprecated
    public enum AuthenticationUserType {
        PERSONAL,
        BUSINESS,
        CORPORATE
    }

    @SuppressWarnings("serial")
    private static class CapabilityList extends ArrayList<Capability> {}

    @SuppressWarnings("serial")
    private static class FieldsList extends ArrayList<Field> {}

    private AccessType accessType;
    @Deprecated private AuthenticationUserType authenticationUserType;
    private AuthenticationFlow authenticationFlow;
    private String capabilitiesSerialized;
    private String className;
    private CredentialsTypes credentialsType;
    private String currency;
    private String displayName;
    private String displayDescription;

    @JsonProperty("fields")
    private String fieldsSerialized;

    private String financialInstitutionId;
    private String financialInstitutionName;

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
    private int backgroundRefreshMaxFrequency;
    private boolean backgroundRefreshEnabled;
    private ProviderStatuses status;
    private boolean transactional;
    private ProviderTypes type;
    private String refreshScheduleSerialized;

    private ReleaseStatus releaseStatus;

    @Nullable private ComplianceStatus complianceStatus;

    private List<FinancialService> financialServices;

    public AccessType getAccessType() {
        return accessType;
    }

    public AuthenticationFlow getAuthenticationFlow() {
        return authenticationFlow;
    }

    @Deprecated
    public AuthenticationUserType getAuthenticationUserType() {
        return authenticationUserType;
    }

    @JsonProperty("capabilities")
    public Set<Capability> getCapabilities() {
        if (Strings.isNullOrEmpty(capabilitiesSerialized)) {
            return Sets.newHashSet();
        }

        return Sets.newConcurrentHashSet(
                SerializationUtils.deserializeFromString(
                        capabilitiesSerialized, CapabilityList.class));
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
        return Optional.ofNullable(getFields())
                .filter(not(List::isEmpty))
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .filter(f -> Objects.equal(f.getName(), name))
                .findFirst()
                .orElse(null);
    }

    private Field getField(final Field.Key key) {
        return getField(key.getFieldKey());
    }

    public List<Field> getFields() {
        return SerializationUtils.deserializeFromString(fieldsSerialized, FieldsList.class);
    }

    public String getFinancialInstitutionId() {
        return financialInstitutionId;
    }

    public String getFinancialInstitutionName() {
        return financialInstitutionName;
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

    public String getTutorialUrl() {
        return tutorialUrl;
    }

    public String getPayload() {
        return payload;
    }

    public int getBackgroundRefreshMaxFrequency() {
        return backgroundRefreshMaxFrequency;
    }

    public boolean isBackgroundRefreshEnabled() {
        return backgroundRefreshEnabled;
    }

    public ProviderStatuses getStatus() {
        return status;
    }

    public List<Field> getSupplementalFields() {
        return SerializationUtils.deserializeFromString(
                supplementalFieldsSerialized, FieldsList.class);
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

    public void setAccessType(AccessType accessType) {
        this.accessType = accessType;
    }

    @Deprecated
    public void setAuthenticationUserType(AuthenticationUserType authenticationUserType) {
        this.authenticationUserType = authenticationUserType;
    }

    public void setAuthenticationFlow(AuthenticationFlow authenticationFlow) {
        this.authenticationFlow = authenticationFlow;
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

    public void setFinancialInstitutionId(String financialInstitutionId) {
        this.financialInstitutionId = financialInstitutionId;
    }

    public void setFinancialInstitutionName(String financialInstitutionName) {
        this.financialInstitutionName = financialInstitutionName;
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

    public void setTutorialUrl(String tutorialUrl) {
        this.tutorialUrl = tutorialUrl;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public void setPopular(boolean popular) {
        this.popular = popular;
    }

    public void setBackgroundRefreshMaxFrequency(int backgroundRefreshMaxFrequency) {
        this.backgroundRefreshMaxFrequency = backgroundRefreshMaxFrequency;
    }

    public void setBackgroundRefreshEnabled(boolean backgroundRefreshEnabled) {
        this.backgroundRefreshEnabled = backgroundRefreshEnabled;
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
        this.supplementalFieldsSerialized =
                SerializationUtils.serializeToString(supplementalFields);
    }

    @JsonProperty("refreshschedule")
    public void setRefreshSchedule(ProviderRefreshSchedule refreshSchedule) {
        this.refreshScheduleSerialized = SerializationUtils.serializeToString(refreshSchedule);
    }

    public ReleaseStatus getReleaseStatus() {
        return releaseStatus;
    }

    public void setReleaseStatus(ReleaseStatus releaseStatus) {
        this.releaseStatus = releaseStatus;
    }

    @Nullable
    public ComplianceStatus getComplianceStatus() {
        return complianceStatus;
    }

    public void setComplianceStatus(@Nullable ComplianceStatus complianceStatus) {
        this.complianceStatus = complianceStatus;
    }

    public List<FinancialService> getFinancialServices() {
        return financialServices;
    }

    public void setFinancialServices(List<FinancialService> financialServices) {
        this.financialServices = financialServices;
    }
}
