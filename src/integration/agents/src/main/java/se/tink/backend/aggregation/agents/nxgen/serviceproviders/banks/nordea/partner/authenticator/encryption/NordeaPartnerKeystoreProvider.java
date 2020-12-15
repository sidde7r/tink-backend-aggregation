package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.authenticator.encryption;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.configuration.NordeaPartnerConfiguration;

public interface NordeaPartnerKeystoreProvider {
    NordeaPartnerKeystore getKeystore(
            NordeaPartnerConfiguration nordeaConfiguration, String clusterId);
}
