package se.tink.backend.aggregation.configuration.guice.modules.agentcapabilities;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilitiesService;
import se.tink.backend.aggregation.api.AgentCapabilitiesResource;
import se.tink.backend.aggregation.resources.agentcapabilities.AgentCapabilitiesResourceImpl;
import se.tink.libraries.jersey.guice.JerseyResourceRegistrar;

@AllArgsConstructor
public final class AgentCapabilitiesModule extends AbstractModule {

    private final JerseyEnvironment jersey;

    @Override
    protected void configure() {
        bind(AgentCapabilitiesService.class).in(Scopes.SINGLETON);
        bind(AgentCapabilitiesResource.class)
                .to(AgentCapabilitiesResourceImpl.class)
                .in(Scopes.SINGLETON);

        JerseyResourceRegistrar.build()
                .binder(binder())
                .jersey(jersey)
                .addResources(AgentCapabilitiesResource.class)
                .bind();
    }
}
