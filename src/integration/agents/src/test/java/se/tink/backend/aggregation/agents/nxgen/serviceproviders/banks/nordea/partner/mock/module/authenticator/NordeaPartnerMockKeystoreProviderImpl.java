package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.mock.module.authenticator;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.authenticator.encryption.NordeaPartnerKeystore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.authenticator.encryption.NordeaPartnerKeystoreProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.configuration.NordeaPartnerConfiguration;

public class NordeaPartnerMockKeystoreProviderImpl implements NordeaPartnerKeystoreProvider {

    @Override
    public NordeaPartnerKeystore getKeystore(
            NordeaPartnerConfiguration nordeaConfiguration, String clusterId) {
        return new NordeaPartnerMockKeystore();
    }
}
