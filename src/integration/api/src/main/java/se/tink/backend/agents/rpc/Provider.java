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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.client.provider_configuration.rpc.ProviderConfiguration;
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

    public static Provider of(ProviderConfiguration providerConfiguration) {
        Provider provider = new Provider();

        provider.setAccessType(
                Provider.AccessType.valueOf(providerConfiguration.getAccessType().name()));
        if (providerConfiguration.getAuthenticationFlow() != null) {
            provider.setAuthenticationFlow(
                    Provider.AuthenticationFlow.valueOf(
                            providerConfiguration.getAuthenticationFlow().name()));
        }
        provider.setClassName(providerConfiguration.getClassName());
        provider.setCredentialsType(
                CredentialsTypes.valueOf(providerConfiguration.getCredentialsType().name()));
        provider.setCurrency(providerConfiguration.getCurrency());
        provider.setDisplayName(providerConfiguration.getDisplayName());
        provider.setFields(
                providerConfiguration.getFields().stream()
                        .map(Field::of)
                        .collect(Collectors.toList()));
        provider.setFinancialInstitutionId(providerConfiguration.getFinancialInstitutionId());
        provider.setMarket(providerConfiguration.getMarket());
        provider.setName(providerConfiguration.getName());
        provider.setPayload(providerConfiguration.getPayload());
        provider.setStatus(ProviderStatuses.valueOf(providerConfiguration.getStatus().name()));
        provider.setSupplementalFields(
                providerConfiguration.getSupplementalFields().stream()
                        .map(Field::of)
                        .collect(Collectors.toList()));
        provider.setType(ProviderTypes.valueOf(providerConfiguration.getType().name()));
        provider.setAuthenticationUserType(
                Provider.AuthenticationUserType.valueOf(
                        providerConfiguration.getAuthenticationUserType().name()));
        provider.setFinancialServices(
                CollectionUtils.emptyIfNull(providerConfiguration.getFinancialServices()).stream()
                        .map(
                                service -> {
                                    FinancialService mappedService = FinancialService.of(service);
                                    if (mappedService == null) {
                                        logger.warn("Could not map financialService: {}", service);
                                    }
                                    return mappedService;
                                })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
        return provider;
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
