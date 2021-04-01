package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.configuration.UkOpenBankingConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.signer.SecretServiceJwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.provider.UkOpenBankingConfigurationProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;

public final class UkOpenBankingModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(UkOpenBankingConfiguration.class)
                .toProvider(UkOpenBankingConfigurationProvider.class)
                .in(Scopes.SINGLETON);
    }

    @Provides
    @Inject
    @Singleton
    private JwtSigner jwtSigner(UkOpenBankingConfiguration openBankingConfiguration) {
        return new SecretServiceJwtSigner(openBankingConfiguration);
    }
}
