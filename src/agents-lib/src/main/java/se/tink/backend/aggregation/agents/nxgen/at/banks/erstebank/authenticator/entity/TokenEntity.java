package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TokenEntity {

    private String token;
    private String type;
    private String expiresIn;

    public TokenEntity(){}

    public TokenEntity(String token, String type, String expiresIn){
        this.token = token;
        this.type = type;
        this.expiresIn = expiresIn;
    }

    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }

    public String getExpiresIn() {
        return expiresIn;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setExpiresIn(String expiresIn) {
        this.expiresIn = expiresIn;
    }
}
