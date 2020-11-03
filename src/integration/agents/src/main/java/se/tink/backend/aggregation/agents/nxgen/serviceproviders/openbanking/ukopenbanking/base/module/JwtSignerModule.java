package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.module;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.jwt.KeySignerProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.iface.JwtSigner;

public class JwtSignerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(JwtSigner.class).toProvider(KeySignerProvider.class).in(Scopes.SINGLETON);
    }
}
