package se.tink.backend.main.guice;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import java.util.List;
import se.tink.backend.main.auth.authenticators.BasicAuthenticator;
import se.tink.backend.main.auth.authenticators.BearerAuthenticator;
import se.tink.backend.main.auth.authenticators.FacebookAuthenticator;
import se.tink.backend.main.auth.authenticators.RequestAuthenticator;
import se.tink.backend.main.auth.authenticators.SessionAuthenticator;
import se.tink.backend.main.auth.authenticators.TokenAuthenticator;
import se.tink.backend.main.providers.ClientProvider;
import se.tink.libraries.auth.HttpAuthenticationMethod;

public class AuthenticationModule extends AbstractModule {

    private List<HttpAuthenticationMethod> httpAuthenticationMethods;

    public AuthenticationModule(List<HttpAuthenticationMethod> httpAuthenticationMethods) {
        Preconditions.checkArgument(httpAuthenticationMethods != null && !httpAuthenticationMethods.isEmpty(),
                "At least one authentication method is required.");
        this.httpAuthenticationMethods = httpAuthenticationMethods;
    }

    @Override
    protected void configure() {
        bind(ClientProvider.class).in(Scopes.SINGLETON);
        bindRequestAuthenticators();
    }

    private void bindRequestAuthenticators() {
        Multibinder<RequestAuthenticator> requestAuthenticatorBinder = Multibinder
                .newSetBinder(binder(), RequestAuthenticator.class);

        boolean atLeastOneValidMethod = false;

        if (httpAuthenticationMethods.contains(HttpAuthenticationMethod.SESSION)) {
            requestAuthenticatorBinder.addBinding().to(SessionAuthenticator.class).in(Scopes.SINGLETON);
            atLeastOneValidMethod = true;
        }

        if (httpAuthenticationMethods.contains(HttpAuthenticationMethod.BASIC)) {
            requestAuthenticatorBinder.addBinding().to(BasicAuthenticator.class).in(Scopes.SINGLETON);
            atLeastOneValidMethod = true;
        }

        if (httpAuthenticationMethods.contains(HttpAuthenticationMethod.TOKEN)) {
            requestAuthenticatorBinder.addBinding().to(TokenAuthenticator.class).in(Scopes.SINGLETON);
            atLeastOneValidMethod = true;
        }

        if (httpAuthenticationMethods.contains(HttpAuthenticationMethod.BEARER)) {
            requestAuthenticatorBinder.addBinding().to(BearerAuthenticator.class).in(Scopes.SINGLETON);
            atLeastOneValidMethod = true;
        }

        if (httpAuthenticationMethods.contains(HttpAuthenticationMethod.FACEBOOK)) {
            requestAuthenticatorBinder.addBinding().to(FacebookAuthenticator.class).in(Scopes.SINGLETON);
            atLeastOneValidMethod = true;
        }

        Preconditions.checkArgument(atLeastOneValidMethod, "At least one valid authentication method is required.");
    }
}
