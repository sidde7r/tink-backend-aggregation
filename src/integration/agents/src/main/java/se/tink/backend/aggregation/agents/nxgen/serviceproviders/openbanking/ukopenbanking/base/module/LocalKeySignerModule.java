package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.module;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.module.provider.LocalKeySignerProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.iface.JwtSigner;

/** @deprecated Please use @{@link JwtSignerModule} */
@Deprecated
public class LocalKeySignerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(JwtSigner.class).toProvider(LocalKeySignerProvider.class).in(Scopes.SINGLETON);
    }
}
