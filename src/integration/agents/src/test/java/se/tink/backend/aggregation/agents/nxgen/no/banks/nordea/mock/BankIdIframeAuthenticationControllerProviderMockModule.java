package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.mock;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.module.loader.TestModule;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeAuthenticationControllerProvider;

@Ignore
public class BankIdIframeAuthenticationControllerProviderMockModule extends TestModule {

    @Override
    public void configure() {
        bind(BankIdIframeAuthenticationControllerProvider.class)
                .toInstance(new BankIdIframeAuthenticationControllerProviderMock());
    }
}
