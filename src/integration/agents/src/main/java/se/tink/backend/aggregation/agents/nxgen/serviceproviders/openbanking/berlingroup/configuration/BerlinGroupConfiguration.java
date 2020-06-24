package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.configuration;

import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

public interface BerlinGroupConfiguration extends ClientConfiguration {

    String getClientId();

    String getClientSecret();

    String getBaseUrl();

    String getPsuIpAddress();
}
