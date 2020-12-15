package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.authenticator.encryption;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.configuration.NordeaPartnerConfiguration;

public class NordeaPartnerKeystoreProviderImpl implements NordeaPartnerKeystoreProvider {

    @Override
    public NordeaPartnerKeystore getKeystore(
            NordeaPartnerConfiguration nordeaConfiguration, String clusterId) {
        return new NordeaPartnerKeystoreImpl(nordeaConfiguration, clusterId);
    }
}
