package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaConfiguration;

public class NordeaSEConfiguration implements NordeaConfiguration {
    @Override
    public String getBaseUrl() {
        return "https://private.nordea.se/api/dbf/";
    }

    @Override
    public boolean isBusinessAgent() {
        return false;
    }

    @Override
    public String getRedirectUri() {
        return "com.nordea.mobilebank.se://auth-callback";
    }

    @Override
    public String getClientId() {
        return "zrIIeWA0LqJHJJ0ZSZr1";
    }
}
