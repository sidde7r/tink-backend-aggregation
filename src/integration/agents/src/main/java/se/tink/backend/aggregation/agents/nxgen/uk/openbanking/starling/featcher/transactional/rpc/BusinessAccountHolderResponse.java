package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BusinessAccountHolderResponse {

    private String companyName;
    private String companyType;
    private String companyCategory;
    private String companySubCategory;
    private String companyRegistrationNumber;
    private String email;
    private String phone;

    public String getCompanyName() {
        return companyName;
    }

    public String getCompanyType() {
        return companyType;
    }

    public String getCompanyCategory() {
        return companyCategory;
    }

    public String getCompanySubCategory() {
        return companySubCategory;
    }

    public String getCompanyRegistrationNumber() {
        return companyRegistrationNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }
}
