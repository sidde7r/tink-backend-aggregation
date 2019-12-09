package se.tink.backend.aggregation.workers.commands.login;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.Agent;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.metrics.MetricActionIface;

public class LoginExecutor {

    private List<LoginHandler> loginHandlerChain = new LinkedList<>();
    private List<LoginExceptionHandler> loginExceptionHandlerChain = new LinkedList<>();

    public LoginExecutor(
            final StatusUpdater statusUpdater,
            final AgentWorkerCommandContext context,
            final SupplementalInformationController supplementalInformationController) {
        initLoginHandlerChain(supplementalInformationController);
        initLoginExceptionHandlerChain(statusUpdater, context);
    }

    private void initLoginHandlerChain(
            final SupplementalInformationController supplementalInformationController) {
        loginHandlerChain.add(
                new ProgressiveAuthenticatorLoginHandler(supplementalInformationController));
        loginHandlerChain.add(new DefaultAgentLoginController());
        loginHandlerChain.add(new LoginFailedHandler());
    }

    private void initLoginExceptionHandlerChain(
            final StatusUpdater statusUpdater, final AgentWorkerCommandContext context) {
        loginExceptionHandlerChain.add(new BankIdLoginExceptionHandler(statusUpdater, context));
        loginExceptionHandlerChain.add(
                new BankServiceLoginExceptionHandler(statusUpdater, context));
        loginExceptionHandlerChain.add(
                new AuthenticationAndAuthorizationLoginExceptionHandler(statusUpdater, context));
        loginExceptionHandlerChain.add(new DefaultLoginExceptionHandler(statusUpdater, context));
    }

    public AgentWorkerCommandResult executeLogin(
            final Agent agent,
            final MetricActionIface metricAction,
            final Credentials credentials) {
        try {
            return processLoginHandlerChain(agent, metricAction, credentials);
        } catch (Exception ex) {
            return processLoginExceptionHandlerChain(ex, metricAction);
        }
    }

    private AgentWorkerCommandResult processLoginHandlerChain(
            final Agent agent, final MetricActionIface metricAction, final Credentials credentials)
            throws Exception {
        for (LoginHandler loginHandler : loginHandlerChain) {
            Optional<AgentWorkerCommandResult> result =
                    loginHandler.handleLogin(agent, metricAction, credentials);
            if (result.isPresent()) {
                return result.get();
            }
        }
        throw new IllegalStateException("There is no more login handlers to process");
    }

    private AgentWorkerCommandResult processLoginExceptionHandlerChain(
            final Exception ex, final MetricActionIface metricAction) {
        for (LoginExceptionHandler loginExceptionHandler : loginExceptionHandlerChain) {
            Optional<AgentWorkerCommandResult> result =
                    loginExceptionHandler.handleLoginException(ex, metricAction);
            if (result.isPresent()) {
                return result.get();
            }
        }
        throw new IllegalStateException("There is no more login exception handlers to process");
    }
}
