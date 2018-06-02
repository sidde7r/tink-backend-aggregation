package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.entities.authentication;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.LinkEntity;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserEntity {
    private String authenticationMethod;
    private Integer authenticationLevel;
    private String defaultBalanceAccountNumberWithoutFallback;
    private String defaultBalanceAccountNumber;
    private String defaultPaymentAccountNumber;
    private String lastLoggedInDate;
    private Integer age;
    private Boolean adult;
    private Boolean child;
    private String bank;
    @JsonProperty("_links")
    private HashMap<String, LinkEntity> links;

    public String getAuthenticationMethod() {
        return authenticationMethod;
    }

    public void setAuthenticationMethod(String authenticationMethod) {
        this.authenticationMethod = authenticationMethod;
    }

    public Integer getAuthenticationLevel() {
        return authenticationLevel;
    }

    public void setAuthenticationLevel(Integer authenticationLevel) {
        this.authenticationLevel = authenticationLevel;
    }

    public String getDefaultBalanceAccountNumberWithoutFallback() {
        return defaultBalanceAccountNumberWithoutFallback;
    }

    public void setDefaultBalanceAccountNumberWithoutFallback(String defaultBalanceAccountNumberWithoutFallback) {
        this.defaultBalanceAccountNumberWithoutFallback = defaultBalanceAccountNumberWithoutFallback;
    }

    public String getDefaultBalanceAccountNumber() {
        return defaultBalanceAccountNumber;
    }

    public void setDefaultBalanceAccountNumber(String defaultBalanceAccountNumber) {
        this.defaultBalanceAccountNumber = defaultBalanceAccountNumber;
    }

    public String getDefaultPaymentAccountNumber() {
        return defaultPaymentAccountNumber;
    }

    public void setDefaultPaymentAccountNumber(String defaultPaymentAccountNumber) {
        this.defaultPaymentAccountNumber = defaultPaymentAccountNumber;
    }

    public String getLastLoggedInDate() {
        return lastLoggedInDate;
    }

    public void setLastLoggedInDate(String lastLoggedInDate) {
        this.lastLoggedInDate = lastLoggedInDate;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Boolean getAdult() {
        return adult;
    }

    public void setAdult(Boolean adult) {
        this.adult = adult;
    }

    public Boolean getChild() {
        return child;
    }

    public void setChild(Boolean child) {
        this.child = child;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public HashMap<String, LinkEntity> getLinks() {
        return links;
    }

    public void setLinks(
            HashMap<String, LinkEntity> links) {
        this.links = links;
    }
}
