package se.tink.backend.aggregation.workers.commands.login;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.agentplatform.authentication.AgentPlatformAuthenticationExecutor;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.workers.commands.login.handler.AgentPlatformAgentLoginHandler;
import se.tink.backend.aggregation.workers.commands.login.handler.CredentialsStatusLoginResultVisitor;
import se.tink.backend.aggregation.workers.commands.login.handler.DefaultLegacyAgentLoginHandler;
import se.tink.backend.aggregation.workers.commands.login.handler.LoginHandler;
import se.tink.backend.aggregation.workers.commands.login.handler.ProgressiveAgentLoginHandler;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginResult;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.credentials.service.CredentialsRequest;
import src.libraries.interaction_counter.InteractionCounter;

public class LoginExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginExecutor.class);

    private final MetricsFactory metricsFactory;
    private final StatusUpdater statusUpdater;
    private List<LoginHandler> loginHandlers;

    public LoginExecutor(final MetricsFactory metricsFactory, final StatusUpdater statusUpdater) {
        this(metricsFactory, statusUpdater, new AgentPlatformAuthenticationExecutor());
    }

    public LoginExecutor(
            MetricsFactory metricsFactory,
            final StatusUpdater statusUpdater,
            final AgentPlatformAuthenticationExecutor agentPlatformAuthenticationExecutor) {
        this.metricsFactory = metricsFactory;
        this.statusUpdater = statusUpdater;
        initLoginHandlers(agentPlatformAuthenticationExecutor);
    }

    private void initLoginHandlers(
            final AgentPlatformAuthenticationExecutor agentPlatformAuthenticationExecutor) {
        loginHandlers = new LinkedList<>();
        loginHandlers.add(new AgentPlatformAgentLoginHandler(agentPlatformAuthenticationExecutor));
        loginHandlers.add(new ProgressiveAgentLoginHandler());
        loginHandlers.add(new DefaultLegacyAgentLoginHandler());
    }

    public AgentWorkerCommandResult execute(
            AgentWorkerCommandContext context,
            SupplementalInformationController supplementalInformationController,
            DataStudioLoginEventPublisherService dataStudioLoginEventPublisherService) {
        for (LoginHandler loginHandler : loginHandlers) {
            Optional<LoginResult> result =
                    loginHandler.handle(
                            context.getAgent(),
                            context.getRequest(),
                            supplementalInformationController);
            if (result.isPresent()) {
                postLoginActions(result.get(), context, dataStudioLoginEventPublisherService);
                return determineAgentWorkerCommandResult(result.get());
            }
        }
        throw new IllegalStateException(
                "Login processor not found for agent type [" + context.getAgent().getClass() + "]");
    }

    private void postLoginActions(
            LoginResult loginResult,
            AgentWorkerCommandContext context,
            DataStudioLoginEventPublisherService dataStudioLoginEventPublisherService) {
        loginResult.accept(new LoggerLoginResultVisitor());
        logUserInteractionStatus(context.getSupplementalInteractionCounter());
        createLoginMetric(
                loginResult, context.getRequest(), context.getSupplementalInteractionCounter());
        updateCredentialsStatus(loginResult, context);
        publishDataStudioLoginEvent(loginResult, dataStudioLoginEventPublisherService);
    }

    private void updateCredentialsStatus(
            LoginResult loginResult, AgentWorkerCommandContext context) {
        loginResult.accept(new CredentialsStatusLoginResultVisitor(statusUpdater, context));
    }

    private void createLoginMetric(
            LoginResult loginResult,
            CredentialsRequest credentialsRequest,
            InteractionCounter supplementalInformationInteractionCounter) {
        loginResult.accept(
                new LoginMetricLoginResultVisitor(
                        metricsFactory.createLoginMetric(
                                credentialsRequest, supplementalInformationInteractionCounter)));
    }

    private void publishDataStudioLoginEvent(
            LoginResult loginResult,
            DataStudioLoginEventPublisherService dataStudioLoginEventPublisherService) {
        loginResult.accept(
                new DataStudioEventPublisherLoginResultVisitor(
                        dataStudioLoginEventPublisherService));
    }

    private void logUserInteractionStatus(InteractionCounter interactionCounter) {
        LOGGER.info(
                "Authentication required user intervention: {}",
                interactionCounter.getNumberInteractions() > 0);
    }

    private AgentWorkerCommandResult determineAgentWorkerCommandResult(LoginResult loginResult) {
        AgentWorkerCommandResultLoginResultVisitor agentWorkerCommandResultLoginResultVisitor =
                new AgentWorkerCommandResultLoginResultVisitor();
        loginResult.accept(agentWorkerCommandResultLoginResultVisitor);
        return agentWorkerCommandResultLoginResultVisitor.getResult();
    }
}
