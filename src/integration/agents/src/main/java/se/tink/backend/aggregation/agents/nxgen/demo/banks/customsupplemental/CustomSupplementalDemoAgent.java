package se.tink.backend.aggregation.agents.nxgen.demo.banks.customsupplemental;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import java.util.Collections;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.customsupplemental.authenticator.CustomSupplementalAuthenticator;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public class CustomSupplementalDemoAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor {

    @Inject
    public CustomSupplementalDemoAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new CustomSupplementalAuthenticator(supplementalInformationController);
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return new FetchAccountsResponse(Collections.emptyList());
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return new FetchTransactionsResponse(Collections.emptyMap());
    }
}
