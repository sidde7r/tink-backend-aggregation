package se.tink.backend.aggregation.configuration.guice.modules.authentication.options;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.authentication.options.AuthenticationOptionsExtractor;
import se.tink.backend.aggregation.api.AuthenticationOptionsService;
import se.tink.backend.aggregation.resources.authentication.options.AuthenticationOptionsResource;
import se.tink.libraries.jersey.guice.JerseyResourceRegistrar;

@AllArgsConstructor
public class AuthenticationOptionsModule extends AbstractModule {

    private final JerseyEnvironment jersey;

    @Override
    protected void configure() {
        bind(AuthenticationOptionsExtractor.class).in(Scopes.SINGLETON);
        bind(AuthenticationOptionsService.class)
                .to(AuthenticationOptionsResource.class)
                .in(Scopes.SINGLETON);

        JerseyResourceRegistrar.build()
                .binder(binder())
                .jersey(jersey)
                .addResources(AuthenticationOptionsService.class)
                .bind();
    }
}
