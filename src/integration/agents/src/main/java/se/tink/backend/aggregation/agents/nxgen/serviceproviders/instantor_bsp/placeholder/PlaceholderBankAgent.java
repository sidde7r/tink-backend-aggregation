package se.tink.backend.aggregation.agents.nxgen.serviceproviders.instantor_bsp.placeholder;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agent.AgentVisitor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

/**
 * This is a placeholder agent that only serves to define capabilities for the PL market. The actual
 * implementation exists only as standalone agent, but the current implementation of provider
 * capabilities demands that we point the provider to an agent in AS with capabilities annotation.
 *
 * <p>TODO: AAP-1241 Remove placeholder when agent platform supports defining capabilities on its
 * own.
 */
@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, IDENTITY_DATA})
public class PlaceholderBankAgent extends NextGenerationAgent
        implements RefreshIdentityDataExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {

    private static final String PLACEHOLDER_AGENT_SHOULD_NEVER_BE_INSTANTIATED =
            "Placeholder agent should never be instantiated.";

    @Inject
    public PlaceholderBankAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        throw new UnsupportedOperationException(PLACEHOLDER_AGENT_SHOULD_NEVER_BE_INSTANTIATED);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        throw new UnsupportedOperationException(PLACEHOLDER_AGENT_SHOULD_NEVER_BE_INSTANTIATED);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        throw new UnsupportedOperationException(PLACEHOLDER_AGENT_SHOULD_NEVER_BE_INSTANTIATED);
    }

    @Override
    public void accept(AgentVisitor visitor) {
        throw new UnsupportedOperationException(PLACEHOLDER_AGENT_SHOULD_NEVER_BE_INSTANTIATED);
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        throw new UnsupportedOperationException(PLACEHOLDER_AGENT_SHOULD_NEVER_BE_INSTANTIATED);
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        throw new UnsupportedOperationException(PLACEHOLDER_AGENT_SHOULD_NEVER_BE_INSTANTIATED);
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        throw new UnsupportedOperationException(PLACEHOLDER_AGENT_SHOULD_NEVER_BE_INSTANTIATED);
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        throw new UnsupportedOperationException(PLACEHOLDER_AGENT_SHOULD_NEVER_BE_INSTANTIATED);
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        throw new UnsupportedOperationException(PLACEHOLDER_AGENT_SHOULD_NEVER_BE_INSTANTIATED);
    }
}
