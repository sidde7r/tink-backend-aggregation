package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginCredentials {

    private UserIdLogin userIdLogin;

    private LoginCredentials(UserIdLogin userIdLogin) {
        this.userIdLogin = userIdLogin;
    }

    public static LoginCredentials createLoginCredentials(String username, String password) {
        UserIdLogin userIdLogin = new UserIdLogin(username, password);
        return new LoginCredentials(userIdLogin);
    }

    public static LoginCredentials createLoginCredentials(
            String username, String password, String rememberMeToken) {
        UserIdLogin userIdLogin = new UserIdLogin(username, password, rememberMeToken);
        return new LoginCredentials(userIdLogin);
    }
}
