package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.barclays.mock.module;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.barclays.mock.fake.FakeJwtSigner;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.iface.JwtSigner;

public final class BarclaysWireMockTestModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(JwtSigner.class).to(FakeJwtSigner.class).in(Scopes.SINGLETON);
    }
}
