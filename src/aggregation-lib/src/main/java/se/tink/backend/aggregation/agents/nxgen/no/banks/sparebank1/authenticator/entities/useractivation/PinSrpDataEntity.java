package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.entities.useractivation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PinSrpDataEntity {
    private String salt;
    private String username;
    private String verificator;

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getVerificator() {
        return verificator;
    }

    public void setVerificator(String verificator) {
        this.verificator = verificator;
    }
}
