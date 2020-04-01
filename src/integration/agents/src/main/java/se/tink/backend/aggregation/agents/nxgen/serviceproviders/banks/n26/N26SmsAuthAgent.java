package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.N26SmsAuthenticator;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.smsotp.SmsOtpAuthenticationPasswordController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class N26SmsAuthAgent extends N26Agent {

    public N26SmsAuthAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        // TODO: Set timeout

        N26SmsAuthenticator authenticator = new N26SmsAuthenticator(sessionStorage, n26APiClient);
        SmsOtpAuthenticationPasswordController<String> thirdPartyAppAuthenticationController =
                new SmsOtpAuthenticationPasswordController(
                        catalog, supplementalInformationHelper, authenticator, 6);
        return thirdPartyAppAuthenticationController;
    }
}
