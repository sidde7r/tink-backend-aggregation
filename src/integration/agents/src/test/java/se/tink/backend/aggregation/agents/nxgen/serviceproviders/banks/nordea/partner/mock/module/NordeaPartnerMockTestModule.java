package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.mock.module;

import se.tink.backend.aggregation.agents.module.loader.TestModule;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.authenticator.encryption.NordeaPartnerKeystoreProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.mock.module.authenticator.NordeaPartnerMockKeystoreProviderImpl;

public class NordeaPartnerMockTestModule extends TestModule {

    @Override
    protected void configure() {
        bind(NordeaPartnerKeystoreProvider.class).to(NordeaPartnerMockKeystoreProviderImpl.class);
    }
}
