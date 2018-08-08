package se.tink.backend.aggregation.agents.nxgen.se.banks.sdcse;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sdcse.parser.SdcSeTransactionParser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.SdcBankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.rpc.CredentialsRequest;

/*
 * Configure market specific client, this is SE
 */
public class SdcSeAgent extends SdcAgent {

    public SdcSeAgent(CredentialsRequest request, AgentContext context, String signatureKeyPath) {
        super(request, context, signatureKeyPath,
                new SdcSeConfiguration(request.getProvider()),
                new SdcSeTransactionParser());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new BankIdAuthenticationController<>(context,
                new SdcBankIdAuthenticator(bankClient, sdcSessionStorage, credentials), true);
    }

    @Override
    protected SdcApiClient createApiClient(SdcConfiguration agentConfiguration) {
        return new SdcApiClient(client, agentConfiguration);
    }
}
