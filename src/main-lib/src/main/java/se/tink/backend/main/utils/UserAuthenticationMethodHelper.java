package se.tink.backend.main.utils;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import se.tink.backend.common.config.AuthenticationConfiguration;
import se.tink.backend.core.User;
import se.tink.libraries.auth.AuthenticationMethod;
import se.tink.libraries.validation.validators.EmailValidator;

public class UserAuthenticationMethodHelper {
    private final AuthenticationConfiguration configuration;

    @Inject
    public UserAuthenticationMethodHelper(AuthenticationConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Return the set of login methods that are available on the market of the user.
     */
    public Set<AuthenticationMethod> getAvailableLoginMethods(User user) {
        List<AuthenticationMethod> methods = configuration.getMarketLoginMethods(user.getProfile().getMarketAsCode());

        if (methods == null) {
            return Collections.emptySet();
        }

        return Sets.newHashSet(methods);
    }

    /**
     * Return a set of login methods that the user can use for login. A user can for example be registered with `BANKID`
     * and cannot use `EMAIL_AND_PASSWORD` even if it is available on the market.
     */
    public Set<AuthenticationMethod> getAuthorizedLoginMethods(User user) {
        Set<AuthenticationMethod> methods = Sets.newHashSet();

        if (!Strings.isNullOrEmpty(user.getNationalId())) {
            methods.add(AuthenticationMethod.BANKID);
        }

        if (EmailValidator.isValid(user.getUsername()) && !Strings.isNullOrEmpty(user.getHash())) {
            methods.add(AuthenticationMethod.EMAIL_AND_PASSWORD);
        }

        // Make sure that the method is available
        return Sets.intersection(getAvailableLoginMethods(user), methods).immutableCopy();
    }
}
