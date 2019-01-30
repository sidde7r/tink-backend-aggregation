package se.tink.backend.aggregation.agents.creditcards.supremecard.v2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDataEntity {
    private String firstName;
    private String middleName;
    private String surename;
    private String email;
    private String telephone;
    private String mobile;
    private Integer numberAccounts;
    private String numberUnreadMessages;
    private String numberMessages;
    private String addressStreet;
    private String addressZipCode;
    private String addressCity;
    private String addressCountry;
    private List<AccountEntity> accounts;
    private String bonusAmount;
    private String upcomingBonusAmount;
    @JsonProperty("login_type")
    private String loginType;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getSurename() {
        return surename;
    }

    public void setSurename(String surename) {
        this.surename = surename;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Integer getNumberAccounts() {
        return numberAccounts;
    }

    public void setNumberAccounts(Integer numberAccounts) {
        this.numberAccounts = numberAccounts;
    }

    public String getNumberUnreadMessages() {
        return numberUnreadMessages;
    }

    public void setNumberUnreadMessages(String numberUnreadMessages) {
        this.numberUnreadMessages = numberUnreadMessages;
    }

    public String getNumberMessages() {
        return numberMessages;
    }

    public void setNumberMessages(String numberMessages) {
        this.numberMessages = numberMessages;
    }

    public String getAddressStreet() {
        return addressStreet;
    }

    public void setAddressStreet(String addressStreet) {
        this.addressStreet = addressStreet;
    }

    public String getAddressZipCode() {
        return addressZipCode;
    }

    public void setAddressZipCode(String addressZipCode) {
        this.addressZipCode = addressZipCode;
    }

    public String getAddressCity() {
        return addressCity;
    }

    public void setAddressCity(String addressCity) {
        this.addressCity = addressCity;
    }

    public String getAddressCountry() {
        return addressCountry;
    }

    public void setAddressCountry(String addressCountry) {
        this.addressCountry = addressCountry;
    }

    public List<AccountEntity> getAccounts() {
        return accounts;
    }

    public void setAccounts(
            List<AccountEntity> accounts) {
        this.accounts = accounts;
    }

    public String getBonusAmount() {
        return bonusAmount;
    }

    public void setBonusAmount(String bonusAmount) {
        this.bonusAmount = bonusAmount;
    }

    public String getUpcomingBonusAmount() {
        return upcomingBonusAmount;
    }

    public void setUpcomingBonusAmount(String upcomingBonusAmount) {
        this.upcomingBonusAmount = upcomingBonusAmount;
    }

    public String getLoginType() {
        return loginType;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }
}
