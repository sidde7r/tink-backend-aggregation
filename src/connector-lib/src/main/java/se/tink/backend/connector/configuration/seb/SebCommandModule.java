package se.tink.backend.connector.configuration.seb;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import se.tink.backend.connector.api.SEBConnectorService;
import se.tink.backend.connector.resources.SEBConnectorServiceResource;

public class SebCommandModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(SEBConnectorService.class).to(SEBConnectorServiceResource.class).in(Scopes.SINGLETON);

    }

}
