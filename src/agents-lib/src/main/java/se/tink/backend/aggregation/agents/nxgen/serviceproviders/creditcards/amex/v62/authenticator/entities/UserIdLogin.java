package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserIdLogin {

  private boolean rememberMeFlag;
  private String userId;
  private String password;
//  private String rememberMeToken;

    public UserIdLogin setRememberMeFlag(boolean rememberMeFlag) {
        this.rememberMeFlag = rememberMeFlag;
        return this;
    }

    public UserIdLogin setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public UserIdLogin setPassword(String password) {
        this.password = password;
        return this;
    }

//    public void setRememberMeToken(String rememberMeToken) {
//        this.rememberMeToken = rememberMeToken;
//    }
}
