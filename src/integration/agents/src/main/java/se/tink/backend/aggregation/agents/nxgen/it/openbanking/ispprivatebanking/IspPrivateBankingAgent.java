package se.tink.backend.aggregation.agents.nxgen.it.openbanking.ispprivatebanking;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.isp.IspAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class IspPrivateBankingAgent extends IspAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    public IspPrivateBankingAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getIntegrationName() {
        return IspPrivateBankingConstants.INTEGRATION_NAME;
    }
}
