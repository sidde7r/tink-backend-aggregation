package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.entities.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
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

    public Integer getAuthenticationLevel() {
        return authenticationLevel;
    }

    public String getDefaultBalanceAccountNumberWithoutFallback() {
        return defaultBalanceAccountNumberWithoutFallback;
    }

    public String getDefaultBalanceAccountNumber() {
        return defaultBalanceAccountNumber;
    }

    public String getDefaultPaymentAccountNumber() {
        return defaultPaymentAccountNumber;
    }

    public String getLastLoggedInDate() {
        return lastLoggedInDate;
    }

    public Integer getAge() {
        return age;
    }

    public Boolean getAdult() {
        return adult;
    }

    public Boolean getChild() {
        return child;
    }

    public String getBank() {
        return bank;
    }

    public HashMap<String, LinkEntity> getLinks() {
        return links;
    }
}
