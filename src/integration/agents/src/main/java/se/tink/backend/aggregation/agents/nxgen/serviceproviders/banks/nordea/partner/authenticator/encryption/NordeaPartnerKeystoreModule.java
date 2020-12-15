package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.authenticator.encryption;

import com.google.inject.AbstractModule;

public class NordeaPartnerKeystoreModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(NordeaPartnerKeystoreProvider.class).to(NordeaPartnerKeystoreProviderImpl.class);
    }
}
