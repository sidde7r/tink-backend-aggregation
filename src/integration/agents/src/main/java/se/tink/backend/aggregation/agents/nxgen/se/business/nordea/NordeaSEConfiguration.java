package se.tink.backend.aggregation.agents.nxgen.se.business.nordea;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaConfiguration;

public class NordeaSEConfiguration implements NordeaConfiguration {

    @Override
    public String getBaseUrl() {
        return "https://corporate.nordea.se/api/dbf/";
    }

    @Override
    public boolean isBusinessAgent() {
        return true;
    }

    @Override
    public String getRedirectUri() {
        return "com.nordea.SMEMobileBank.se://auth-callback";
    }

    @Override
    public String getClientId() {
        return "Hjh7wsmPVojMkPioAvky";
    }
}
