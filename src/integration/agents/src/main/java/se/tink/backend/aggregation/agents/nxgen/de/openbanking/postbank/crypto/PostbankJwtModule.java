package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.crypto;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class PostbankJwtModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(JwtGenerator.class).to(PostbankJwtGenerator.class).in(Scopes.SINGLETON);
    }
}
