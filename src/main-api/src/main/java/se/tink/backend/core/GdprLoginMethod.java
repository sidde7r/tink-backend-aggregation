package se.tink.backend.core;

import java.util.List;
import se.tink.libraries.auth.AuthenticationMethod;

public class GdprLoginMethod {
    private Market.Code code;
    private String description;
    private List<AuthenticationMethod> authenticationMethod;

    public GdprLoginMethod(Market.Code code, String description, List<AuthenticationMethod> authenticationMethod) {
        this.code = code;
        this.description = description;
        this.authenticationMethod = authenticationMethod;

    }

    public String getDescription() {
        return description;
    }

    public Market.Code getCode() {
        return code;
    }

    public List<AuthenticationMethod> getAuthenticationMethod() {
        return authenticationMethod;
    }
}
