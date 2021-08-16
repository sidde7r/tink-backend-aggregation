package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss;

import com.google.inject.AbstractModule;

public class NemIdIFrameControllerInitializerModule extends AbstractModule {

    @Override
    public void configure() {
        bind(NemIdIFrameControllerInitializer.class)
                .toInstance(new NemIdIFrameControllerInitializerImpl());
    }
}
