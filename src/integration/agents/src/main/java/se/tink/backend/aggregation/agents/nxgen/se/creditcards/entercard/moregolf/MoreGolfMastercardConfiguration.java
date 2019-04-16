package se.tink.backend.aggregation.agents.nxgen.se.creditcards.entercard.moregolf;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.EnterCardConfiguration;

public class MoreGolfMastercardConfiguration extends EnterCardConfiguration {

    @Override
    public String getServiceHost() {
        return "https://minasidor.golf.se";
    }

    @Override
    public String getAuthUrl() {
        return getServiceHost() + "/darwin/api/se/golf/auth/verify";
    }

    @Override
    protected String getSignicatTemplateName() {
        return "ecnb-golf";
    }

    @Override
    public String getJsonVendorMime() {
        return "application/vnd.se.entercard.golf-v1.0+json";
    }
}
