package se.tink.backend.auth;

import com.google.common.base.Preconditions;
import se.tink.backend.core.User;
import se.tink.libraries.auth.HttpAuthenticationMethod;

public class AuthenticatedUser {
    private HttpAuthenticationMethod method;
    private String oAuthClientId;
    private User user;
    private final boolean administrativeMode;

    public AuthenticatedUser(HttpAuthenticationMethod method, User user) {
        this(method, null, user, false);
    }

    public AuthenticatedUser(HttpAuthenticationMethod method, String oAuthClientId, User user) {
        this(method, oAuthClientId, user, false);
    }

    public AuthenticatedUser(HttpAuthenticationMethod method, User user, boolean administrativeMode) {
        this(method, null, user, administrativeMode);
    }
    
    public AuthenticatedUser(HttpAuthenticationMethod method, String oAuthClientId, User user, boolean administrativeMode) {

        Preconditions.checkArgument(method.isValid());

        this.method = method;
        this.oAuthClientId = oAuthClientId;
        this.user = Preconditions.checkNotNull(user);
        this.administrativeMode = administrativeMode;
    }

    public boolean isAdministrativeMode() {
        return administrativeMode;
    }

    public User getUser() {
        return user;
    }

    public String getOAuthClientId() {
        return oAuthClientId;
    }

    public HttpAuthenticationMethod getMethod() {
        return method;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getLocale() {
        return getUser().getLocale();
    }
}
