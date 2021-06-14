package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.wiremock.payment.module;

import com.google.inject.Scopes;
import se.tink.backend.aggregation.agents.module.loader.TestModule;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.crypto.JwtGenerator;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.crypto.PostbankFakeJwtGenerator;

public class PostbankFakeJwtModule extends TestModule {

    @Override
    protected void configure() {
        bind(JwtGenerator.class).to(PostbankFakeJwtGenerator.class).in(Scopes.SINGLETON);
    }
}
