package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.wiremock.module;

import com.google.inject.Scopes;
import se.tink.backend.aggregation.agents.module.loader.TestModule;
import se.tink.backend.aggregation.eidassigner.FakeQsealcSigner;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;

public final class CrosskeyWireMockTestModule extends TestModule {

    @Override
    protected void configure() {
        bind(QsealcSigner.class).to(FakeQsealcSigner.class).in(Scopes.SINGLETON);
    }
}
