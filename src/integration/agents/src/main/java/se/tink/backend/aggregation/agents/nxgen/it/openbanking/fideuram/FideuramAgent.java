package se.tink.backend.aggregation.agents.nxgen.it.openbanking.fideuram;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.isp.IspAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class FideuramAgent extends IspAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    public FideuramAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getIntegrationName() {
        return FideuramConstants.INTEGRATION_NAME;
    }

}
