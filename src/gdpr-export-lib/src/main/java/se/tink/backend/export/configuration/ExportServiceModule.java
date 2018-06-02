package se.tink.backend.export.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import se.tink.backend.export.GdprExportService;
import se.tink.backend.export.GdprExportServiceJerseyTransport;
import se.tink.libraries.jersey.guice.JerseyResourceRegistrar;

public class ExportServiceModule extends AbstractModule {
    private final JerseyEnvironment environment;

    public ExportServiceModule(JerseyEnvironment environment){
        this.environment = environment;
    }

    @Override
    protected void configure() {
        bind(GdprExportService.class).to(GdprExportServiceJerseyTransport.class).in(Scopes.SINGLETON);

        JerseyResourceRegistrar.build()
                .binder(binder())
                .jersey(environment)
                .addResources(
                        GdprExportService.class)
                .bind();
    }
}
