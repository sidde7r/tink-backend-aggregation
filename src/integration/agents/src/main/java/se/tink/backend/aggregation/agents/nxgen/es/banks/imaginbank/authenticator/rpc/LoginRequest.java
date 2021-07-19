package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginRequest {

    @JsonProperty("ima")
    private String username;

    private String userType;

    private boolean existsUser;

    private String password;

    private boolean altaImagine;

    private boolean demo;

    public LoginRequest(
            String username,
            String userType,
            boolean existUser,
            String password,
            boolean altaImagine,
            boolean demo) {
        this.username = username;
        this.userType = userType;
        this.existsUser = existUser;
        this.password = password;
        this.altaImagine = altaImagine;
        this.demo = demo;
    }
}
