package se.tink.backend.export.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import se.tink.backend.export.controller.ExportController;
import se.tink.backend.export.factory.ExportObjectFactory;
import se.tink.backend.export.factory.ExportObjectFactoryImpl;

public class ExportUserDataModule extends AbstractModule {

    public ExportUserDataModule(){
    }

    @Override
    protected void configure() {
        bind(ExportObjectFactory.class).to(ExportObjectFactoryImpl.class).in(Scopes.SINGLETON);
        bind(ExportController.class).in(Scopes.SINGLETON);
    }
}
