package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.mock.module;

import se.tink.backend.aggregation.agents.module.loader.TestModule;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.module.EncapClientProvider;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.module.MockEncapClientProvider;

public final class SparebankenSorWireMockTestModule extends TestModule {

    @Override
    protected void configure() {
        bind(EncapClientProvider.class).toInstance(new MockEncapClientProvider());
    }
}
