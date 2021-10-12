package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.mock.module;

import com.google.inject.Scopes;
import se.tink.backend.aggregation.agents.module.loader.TestModule;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fallback.utils.FakeSignatureUtils;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fallback.utils.SignatureProvider;

public final class SwedbankFallbackWireMockTestModule extends TestModule {

    @Override
    protected void configure() {
        bind(SignatureProvider.class).to(FakeSignatureUtils.class).in(Scopes.SINGLETON);
    }
}
