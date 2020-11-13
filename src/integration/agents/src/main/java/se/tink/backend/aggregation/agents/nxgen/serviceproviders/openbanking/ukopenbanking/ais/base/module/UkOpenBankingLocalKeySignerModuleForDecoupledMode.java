package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.fake.FakeJwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;

public class UkOpenBankingLocalKeySignerModuleForDecoupledMode extends AbstractModule {

    @Override
    protected void configure() {
        bind(JwtSigner.class).to(FakeJwtSigner.class).in(Scopes.SINGLETON);
    }
}
