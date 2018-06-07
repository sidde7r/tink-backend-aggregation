package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginCredentials {

    private UserIdLogin userIdLogin;

    public LoginCredentials setUserIdLogin(
            UserIdLogin userIdLogin) {
        this.userIdLogin = userIdLogin;
        return this;
    }

    public void setUsernameAndPassword(String username, String password) {
        this.userIdLogin = new UserIdLogin();
        this.userIdLogin.setUserId(username)
                .setPassword(password)
                .setRememberMeFlag(true);
    }
}
