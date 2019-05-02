package se.tink.backend.aggregation.agents.nxgen.se.creditcards.entercard.remember;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.EnterCardConfiguration;

public class RememberConfiguration extends EnterCardConfiguration {

    @Override
    public String getServiceHost() {
        return "https://minasidor.remember.se";
    }

    @Override
    public String getAuthUrl() {
        return getServiceHost() + "/darwin/api/se/remember/auth/verify";
    }

    @Override
    protected String getSignicatTemplateName() {
        return "ecnb-remember";
    }

    @Override
    public String getJsonVendorMime() {
        return "application/vnd.se.entercard.remember-v1.0+json";
    }
}
