package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.N26SmsAuthenticator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.smsotp.SmsOtpAuthenticationPasswordController;

public class N26SmsAuthAgent extends N26Agent {

    @Inject
    public N26SmsAuthAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        // TODO: Set timeout

        N26SmsAuthenticator authenticator = new N26SmsAuthenticator(sessionStorage, n26APiClient);
        SmsOtpAuthenticationPasswordController<String> thirdPartyAppAuthenticationController =
                new SmsOtpAuthenticationPasswordController<>(
                        catalog, supplementalInformationHelper, authenticator, 6);
        return thirdPartyAppAuthenticationController;
    }
}
