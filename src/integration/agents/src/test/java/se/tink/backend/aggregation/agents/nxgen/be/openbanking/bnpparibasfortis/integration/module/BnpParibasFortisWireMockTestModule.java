package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.integration.module;

import com.google.inject.Scopes;
import se.tink.backend.aggregation.agents.module.loader.TestModule;
import se.tink.backend.aggregation.eidassigner.FakeQsealcSigner;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;

public class BnpParibasFortisWireMockTestModule extends TestModule {
    @Override
    protected void configure() {
        bind(QsealcSigner.class).to(FakeQsealcSigner.class).in(Scopes.SINGLETON);
    }
}
