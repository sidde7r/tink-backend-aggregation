package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur;

import com.google.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.CajasurAuthenticationApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.CajasurAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login.virtualkeyboardocr.PasswordVirtualKeyboardOcr;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class CajasurAgent extends SubsequentProgressiveGenerationAgent
        implements RefreshCheckingAccountsExecutor {

    @Inject
    protected CajasurAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        return new CajasurAuthenticator(
                new CajasurAuthenticationApiClient(
                        "https://portal.cajasur.es", client, new PasswordVirtualKeyboardOcr()),
                sessionStorage);
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return new FetchAccountsResponse(Collections.emptyList());
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return new FetchTransactionsResponse(new HashMap<>());
    }
}
