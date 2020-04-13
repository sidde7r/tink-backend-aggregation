package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_EMPTY)
public class UserIdLogin {

    private boolean rememberMeFlag = true;
    private String userId;
    private String password;
    private String rememberMeToken;

    public UserIdLogin(String userId, String password) {
        this.userId = userId;
        this.password = password;
    }

    public UserIdLogin(String userId, String password, String rememberMeToken) {
        this.userId = userId;
        this.password = password;
        this.rememberMeToken = rememberMeToken;
    }
}
