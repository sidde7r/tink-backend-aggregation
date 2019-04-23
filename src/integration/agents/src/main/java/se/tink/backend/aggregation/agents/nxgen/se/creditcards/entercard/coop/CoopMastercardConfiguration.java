package se.tink.backend.aggregation.agents.nxgen.se.creditcards.entercard.coop;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.EnterCardConfiguration;

public class CoopMastercardConfiguration extends EnterCardConfiguration {

    @Override
    public String getServiceHost() {
        return "https://coopmastercard.coop.se";
    }

    @Override
    public String getAuthUrl() {
        return getServiceHost() + "/darwin/api/se/coop/auth/verify";
    }

    @Override
    public String getSignicatTemplateName() {
        return "ecnb-coop-se";
    }

    @Override
    public String getJsonVendorMime() {
        return "application/vnd.se.entercard.coop-v1.0+json";
    }
}
