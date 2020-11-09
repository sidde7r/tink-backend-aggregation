package se.tink.backend.aggregation.workers.commands.login;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.MetricActionIface;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class LoginExecutor {

    private static final Logger log = LoggerFactory.getLogger(LoginExecutor.class);
    private List<LoginHandler> loginHandlerChain = new LinkedList<>();
    private List<LoginExceptionHandler> loginExceptionHandlerChain = new LinkedList<>();
    private AgentWorkerCommandContext context;
    private SupplementalInformationController supplementalInformationController;
    private AgentLoginEventPublisherService agentLoginEventPublisherService;

    public LoginExecutor(
            final StatusUpdater statusUpdater,
            final AgentWorkerCommandContext context,
            final SupplementalInformationController supplementalInformationController,
            final AgentLoginEventPublisherService agentLoginEventPublisherService) {
        this.supplementalInformationController = supplementalInformationController;
        this.agentLoginEventPublisherService = agentLoginEventPublisherService;
        this.context = context;
        initLoginHandlerChain();
        initLoginExceptionHandlerChain(statusUpdater);
    }

    private void initLoginHandlerChain() {
        loginHandlerChain.add(
                new ProgressiveAuthenticatorLoginHandler(
                        supplementalInformationController, agentLoginEventPublisherService));
        loginHandlerChain.add(new DefaultAgentLoginController(agentLoginEventPublisherService));
        loginHandlerChain.add(new LoginFailedHandler());
    }

    private void initLoginExceptionHandlerChain(final StatusUpdater statusUpdater) {
        loginExceptionHandlerChain.add(
                new BankIdLoginExceptionHandler(
                        statusUpdater, context, agentLoginEventPublisherService));
        loginExceptionHandlerChain.add(
                new BankServiceLoginExceptionHandler(
                        statusUpdater, context, agentLoginEventPublisherService));
        loginExceptionHandlerChain.add(
                new AuthenticationLoginExceptionHandler(
                        statusUpdater, context, agentLoginEventPublisherService));
        loginExceptionHandlerChain.add(
                new AuthorizationLoginExceptionHandler(
                        statusUpdater, context, agentLoginEventPublisherService));
        loginExceptionHandlerChain.add(
                new DefaultLoginExceptionHandler(
                        statusUpdater, context, agentLoginEventPublisherService));
    }

    public AgentWorkerCommandResult executeLogin(
            final Agent agent,
            final MetricActionIface metricAction,
            final CredentialsRequest credentialsRequest) {
        try {
            log.info(
                    "LoginExecutor for credentials {}",
                    Optional.ofNullable(credentialsRequest.getCredentials())
                            .map(Credentials::getId)
                            .orElse(null));
            return processLoginHandlerChain(agent, metricAction, credentialsRequest);
        } catch (Exception ex) {
            log.info(
                    "LoginExecutor-EXCEPTION for credentials {}",
                    Optional.ofNullable(credentialsRequest.getCredentials())
                            .map(Credentials::getId)
                            .orElse(null),
                    ex);
            return processLoginExceptionHandlerChain(ex, metricAction);
        }
    }

    private AgentWorkerCommandResult processLoginHandlerChain(
            final Agent agent,
            final MetricActionIface metricAction,
            final CredentialsRequest credentialsRequest)
            throws Exception {
        for (LoginHandler loginHandler : loginHandlerChain) {
            Optional<AgentWorkerCommandResult> result =
                    loginHandler.handleLogin(agent, metricAction, credentialsRequest);
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
