package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserPersonEntity {
    private int id;
    private String created;
    private String updated;
    private String status;
    @JsonProperty("sub_status")
    private String subStatus;
    @JsonProperty("public_uuid")
    private String publicUuid;
    @JsonProperty("display_name")
    private String displayName;
    @JsonProperty("public_nick_name")
    private String publicNickName;
    private String language;
    private String region;
    @JsonProperty("session_timeout")
    private int sessionTimeout;
    @JsonProperty("first_name")
    private String firstName;
    @JsonProperty("middle_name")
    private String middleName;
    @JsonProperty("last_name")
    private String lastName;
    @JsonProperty("legal_name")
    private String legalName;
    @JsonProperty("date_of_birth")
    private String dateOfBirth;
    @JsonProperty("place_of_birth")
    private String placeOfBirth;
    @JsonProperty("country_of_birth")
    private String countryOfBirth;
    private String nationality;
    private String gender;
    @JsonProperty("version_terms_of_service")
    private String versionTermsOfService;
    @JsonProperty("document_number")
    private String documentNumber;
    @JsonProperty("document_type")
    private String documentType;
    @JsonProperty("document_country_of_issuance")
    private String documentCountryOfIssuance;
    private CustomerEntity customer;
    @JsonProperty("customer_limit")
    private CustomerLimitEntity customerLimit;

    public int getId() {
        return id;
    }

    public String getCreated() {
        return created;
    }

    public String getUpdated() {
        return updated;
    }

    public String getStatus() {
        return status;
    }

    public String getSubStatus() {
        return subStatus;
    }

    public String getPublicUuid() {
        return publicUuid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPublicNickName() {
        return publicNickName;
    }

    public String getLanguage() {
        return language;
    }

    public String getRegion() {
        return region;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getLegalName() {
        return legalName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getPlaceOfBirth() {
        return placeOfBirth;
    }

    public String getCountryOfBirth() {
        return countryOfBirth;
    }

    public String getNationality() {
        return nationality;
    }

    public String getGender() {
        return gender;
    }

    public String getVersionTermsOfService() {
        return versionTermsOfService;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public String getDocumentType() {
        return documentType;
    }

    public String getDocumentCountryOfIssuance() {
        return documentCountryOfIssuance;
    }

    public CustomerEntity getCustomer() {
        return customer;
    }

    public CustomerLimitEntity getCustomerLimit() {
        return customerLimit;
    }
}
