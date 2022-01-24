package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid;

import com.google.inject.AbstractModule;

public class MitIdAuthenticationControllerProviderModule extends AbstractModule {

    @Override
    public void configure() {
        bind(MitIdAuthenticationControllerProvider.class)
                .toInstance(new MitIdAuthenticationControllerProviderImpl());
    }
}
