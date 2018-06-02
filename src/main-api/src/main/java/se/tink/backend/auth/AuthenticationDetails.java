package se.tink.backend.auth;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang.NotImplementedException;
import se.tink.libraries.auth.HttpAuthenticationMethod;

public class AuthenticationDetails {
    private final boolean valid;
    private HttpAuthenticationMethod method;
    private String authorizationCredentials;

    public AuthenticationDetails(String authorizationHttpHeader) {
        valid = parse(authorizationHttpHeader);
    }

    public AuthenticationDetails(HttpAuthenticationMethod method, String authorizationCredentials) {
        switch(method) {
        case BASIC:
        case BEARER:
        case FACEBOOK:
        case SESSION:
        case TOKEN:
            if (!Strings.isNullOrEmpty(authorizationCredentials)) {
                this.method = method;
                this.authorizationCredentials = authorizationCredentials;
                this.valid = true;
                break;
            }
            // fall through
        case NON_VALID:
            valid = false;
            break;
        default:
            valid = false;
            throw new NotImplementedException("Not implemented, developer should take action!");
        }
    }

    public HttpAuthenticationMethod getMethod() {
        Preconditions.checkArgument(valid);
        return method;
    }

    public String getAuthorizationCredentials() {
        Preconditions.checkArgument(valid);
        return authorizationCredentials;
    }

    public String getAuthorizationHeaderValue() {
        Preconditions.checkArgument(valid);
        return String.format("%s %s", method.getMethod(), authorizationCredentials);
    }

    public boolean isValid() {
        return valid;
    }

    public BasicAuthenticationDetails getBasicAuthenticationDetails() {
        Preconditions.checkArgument(valid);
        Preconditions.checkArgument(Objects.equals(method, HttpAuthenticationMethod.BASIC));

        return new BasicAuthenticationDetails(authorizationCredentials);
    }

    public Optional<String> getSessionId() {
        if (valid && Objects.equals(method, HttpAuthenticationMethod.SESSION)) {
            return Optional.ofNullable(authorizationCredentials);
        }
        return Optional.empty();
    }

    private boolean parse(String authorizationHttpHeader) {
        int delimiterIndex = authorizationHttpHeader.indexOf(' ');

        if (delimiterIndex < 1) {
            return false;
        }

        method = HttpAuthenticationMethod.fromMethod(authorizationHttpHeader.substring(0, delimiterIndex));
        authorizationCredentials = authorizationHttpHeader.substring(delimiterIndex + 1);

        return method != HttpAuthenticationMethod.NON_VALID;
    }
}
