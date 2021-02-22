package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.signer;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.configuration.DanskebankEUConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.configuration.DanskebankEUConfigurationProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;

public class DanskeOpenBankingModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(DanskebankEUConfiguration.class)
                .toProvider(DanskebankEUConfigurationProvider.class)
                .in(Scopes.SINGLETON);
        bind(JwtSigner.class).toProvider(DanskeJwtSignerProvider.class).in(Scopes.SINGLETON);
    }
}
