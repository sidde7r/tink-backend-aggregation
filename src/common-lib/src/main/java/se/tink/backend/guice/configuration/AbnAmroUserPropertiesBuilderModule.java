package se.tink.backend.guice.configuration;

import com.google.inject.AbstractModule;
import se.tink.backend.common.utils.AbnArmoUserPropertiesBuilder;
import se.tink.backend.common.utils.UserPropertiesBuilder;

public class AbnAmroUserPropertiesBuilderModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(UserPropertiesBuilder.class).to(AbnArmoUserPropertiesBuilder.class);
    }
}
