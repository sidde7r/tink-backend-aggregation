package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.module;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.configuration.UkOpenBankingConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.module.provider.LocalKeySignerProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.module.provider.UkOpenBankingConfigurationProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.iface.JwtSigner;

public final class UkOpenBankingLocalKeySignerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(UkOpenBankingConfiguration.class)
                .toProvider(UkOpenBankingConfigurationProvider.class)
                .in(Scopes.SINGLETON);
        bind(JwtSigner.class).toProvider(LocalKeySignerProvider.class).in(Scopes.SINGLETON);
    }
}
