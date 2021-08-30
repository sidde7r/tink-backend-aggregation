package se.tink.backend.aggregation.agents.nxgen.be.openbanking.beobank.wiremock.ais;

import com.google.inject.Scopes;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.module.loader.TestModule;
import se.tink.backend.aggregation.eidassigner.FakeQsealcSigner;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;

@Ignore
public class BeoBankWireMockTestModuleConfig extends TestModule {
    @Override
    protected void configure() {
        bind(QsealcSigner.class).to(FakeQsealcSigner.class).in(Scopes.SINGLETON);
    }
}
