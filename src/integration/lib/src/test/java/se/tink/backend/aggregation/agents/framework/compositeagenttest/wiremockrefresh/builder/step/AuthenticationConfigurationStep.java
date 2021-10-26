package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.builder.step;

import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;

public interface AuthenticationConfigurationStep {

    /**
     * This is only declaration about the authentication flow of the executed test. It does not
     * assure that full (manual) authentication flow will be executed. When using {@link
     * AutoAuthenticationController}, it will force manual authentication.
     */
    RefreshOrAuthOnlyStep testFullAuthentication();

    /**
     * This is only declaration about the authentication flow of the executed test. It does not
     * assure that auto authentication flow will be executed
     */
    RefreshOrAuthOnlyStep testAutoAuthentication();

    /**
     * Assures that authentication won't be executed. Test should be aware of that and properly mock
     * authentication results.
     */
    RefreshOrAuthOnlyStep skipAuthentication();
}
