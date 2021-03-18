package se.tink.backend.aggregation.agents.nxgen.no.banks.bankidtest;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;

import com.google.inject.Inject;
import java.util.Collections;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

// temporary agent to show BankID iframe controller solution
@AgentCapabilities({CHECKING_ACCOUNTS})
public class BankIdTestAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor {

    @Inject
    public BankIdTestAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return new FetchAccountsResponse(Collections.emptyList());
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return new FetchTransactionsResponse(null);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        BankIdTestIframeInitializer iframeInitializer = new BankIdTestIframeInitializer();
        BankIdTestAuthenticator authenticator = new BankIdTestAuthenticator();

        return BankIdIframeAuthenticationController.authenticationController(
                catalog,
                context,
                supplementalInformationController,
                iframeInitializer,
                authenticator);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
