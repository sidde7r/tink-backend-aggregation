package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fallback;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fallback.utils.SignatureProvider;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fallback.utils.SignatureUtils;

public class SwedbankFallbackModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(SignatureProvider.class).to(SignatureUtils.class).in(Scopes.SINGLETON);
    }
}
