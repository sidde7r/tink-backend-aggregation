package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.fetcher.rpc;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@SuppressWarnings({"unused", "WeakerAccess"})
@JsonObject
public class User {

    protected String name;
    protected String data;
    protected List<Product> products = null;
    protected Integer age;
    protected String email;
    protected String phone;
    protected Boolean isFirstTimeAccess;
    protected Boolean isContactInfoUpdated;
    protected Boolean isEligibleLoan;
    protected Boolean kycUser;
    protected Boolean kycHighRiskUser;
    protected Boolean hideTermsAndConditions;
    protected Boolean isUserHavingMigratedAccount;

    public String getName() {
        return name;
    }

    public String getData() {
        return data;
    }

    public List<Product> getProducts() {
        return products == null ? new ArrayList<>() : products;
    }

    public Integer getAge() {
        return age;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public Boolean getFirstTimeAccess() {
        return isFirstTimeAccess;
    }

    public Boolean getContactInfoUpdated() {
        return isContactInfoUpdated;
    }

    public Boolean getEligibleLoan() {
        return isEligibleLoan;
    }

    public Boolean getKycUser() {
        return kycUser;
    }

    public Boolean getKycHighRiskUser() {
        return kycHighRiskUser;
    }

    public Boolean getHideTermsAndConditions() {
        return hideTermsAndConditions;
    }

    public Boolean getUserHavingMigratedAccount() {
        return isUserHavingMigratedAccount;
    }
}
