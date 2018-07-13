package se.tink.backend.aggregation.agents.nxgen.dk.banks.sdcdk;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.sdcdk.parser.SdcDkTransactionParser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.SdcPinAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.rpc.CredentialsRequest;

/*
 * Configure market specific client, this is DK
 */
public class SdcDkAgent extends SdcAgent {

    public SdcDkAgent(CredentialsRequest request, AgentContext context) {
        super(request, context,
                new SdcDkConfiguration(request.getProvider()),
                new SdcDkTransactionParser());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        SdcPinAuthenticator dkAuthenticator = new SdcPinAuthenticator(bankClient,
                sdcSessionStorage, agentConfiguration);
        return new PasswordAuthenticationController(dkAuthenticator);
    }

    @Override
    protected SdcApiClient createApiClient(SdcConfiguration agentConfiguration) {
        return new SdcApiClient(client, agentConfiguration);
    }
}
