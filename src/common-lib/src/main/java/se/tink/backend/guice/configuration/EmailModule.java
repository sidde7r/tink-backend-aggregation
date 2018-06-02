package se.tink.backend.guice.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.microtripit.mandrillapp.lutung.MandrillApi;
import se.tink.backend.common.config.EmailConfiguration;
import se.tink.backend.common.mail.MailSender;
import se.tink.backend.common.mail.SubscriptionHelper;

public class EmailModule extends AbstractModule {

    private final EmailConfiguration configuration;

    public EmailModule(EmailConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        bind(SubscriptionHelper.class).in(Scopes.SINGLETON);
        bind(MailSender.class).in(Scopes.SINGLETON);
    }

    @Provides
    @Singleton
    public MandrillApi provideMandrillApi() {
        return new MandrillApi(configuration.getMandrillApiKey());
    }
}
