package se.tink.backend.insights.configuration;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.microtripit.mandrillapp.lutung.MandrillApi;
import java.util.List;
import se.tink.backend.common.mail.MailSender;
import se.tink.backend.common.mail.SubscriptionHelper;
import se.tink.backend.main.auth.authenticators.BasicAuthenticator;
import se.tink.backend.main.auth.authenticators.RequestAuthenticator;
import se.tink.backend.main.auth.authenticators.SessionAuthenticator;
import se.tink.backend.main.providers.ClientProvider;
import se.tink.libraries.auth.HttpAuthenticationMethod;

public class SessionAuthenticationModule extends AbstractModule {

    private List<HttpAuthenticationMethod> httpAuthenticationMethods;
    private final String mandrillApiKey;

    public SessionAuthenticationModule(String mandrillApiKey,
            List<HttpAuthenticationMethod> httpAuthenticationMethods) {
        Preconditions.checkArgument(httpAuthenticationMethods != null && !httpAuthenticationMethods.isEmpty(),
                "At least one authentication method is required.");
        this.httpAuthenticationMethods = httpAuthenticationMethods;
        this.mandrillApiKey = mandrillApiKey;
    }

    @Override
    protected void configure() {
        bindConstant().annotatedWith(Names.named("productionMode")).to(false);

        bind(ClientProvider.class).in(Scopes.SINGLETON);
        bind(SubscriptionHelper.class).in(Scopes.SINGLETON);
        bind(MailSender.class).in(Scopes.SINGLETON);
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

        Preconditions.checkArgument(atLeastOneValidMethod, "At least one valid authentication method is required.");
    }

    @Provides
    @Singleton
    public MandrillApi provideMandrillApi() {
        return new MandrillApi(mandrillApiKey);
    }
}
