package se.tink.backend.guice.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import se.tink.backend.common.workers.notifications.channels.MobileNotificationSender;
import se.tink.backend.common.workers.notifications.channels.MobileNotificationSenderImpl;

public class CommandModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(MobileNotificationSender.class).to(MobileNotificationSenderImpl.class).in(Scopes.SINGLETON);
    }
}
