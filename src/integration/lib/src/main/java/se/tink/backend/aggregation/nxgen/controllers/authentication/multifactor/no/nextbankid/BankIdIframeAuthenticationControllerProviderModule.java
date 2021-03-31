package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid;

import com.google.inject.AbstractModule;

public class BankIdIframeAuthenticationControllerProviderModule extends AbstractModule {

    @Override
    public void configure() {
        bind(BankIdIframeAuthenticationControllerProvider.class)
                .toInstance(new BankIdIframeAuthenticationControllerProviderImpl());
    }
}
