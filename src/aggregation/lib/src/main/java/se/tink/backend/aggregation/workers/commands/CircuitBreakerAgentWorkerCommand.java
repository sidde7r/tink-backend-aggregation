package se.tink.backend.aggregation.workers.commands;

import com.google.common.base.Objects;
import com.google.common.util.concurrent.RateLimiter;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.configuration.agentsservice.CircuitBreakerConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.CircuitBreakerMode;
import se.tink.backend.aggregation.workers.commands.state.CircuitBreakerAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.concurrency.CircuitBreakerStatistics;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.connectivity.errors.ConnectivityErrorDetails.ProviderErrors;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.i18n_aggregation.Catalog;
import src.libraries.connectivity_errors.ConnectivityErrorFactory;

/**
 * A command that makes sure to stop continuing an operation if it has failed too many times
 * recently.
 *
 * <p>This was introduced for the following reasons:
 *
 * <ul>
 *   <li>To avoid hammering a broken bank. In other words "stop kicking someone already on the
 *       ground". :) This is to protect us from bringing down a bank.
 *   <li>To avoid filling up our thread pool if banks are timing. We should have timeouts for our
 *       HTTP clients and the monitoring thread should kill something not making progress. That
 *       said, if we are putting a lot of traffic through timeouts could still fill up the thread
 *       pool.
 * </ul>
 *
 * That is, operations that are high-throughput should probably use a circuit breaker.
 */
public class CircuitBreakerAgentWorkerCommand extends AgentWorkerCommand {
    // This is a temporary fix/experiment to skip CircuitBreaker for the Snoop app.
    // Do NOT add more appIds here.
    private static final String SNOOP_APP_ID = "a6a018f4f823493db857943db6fad7df";

    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private CircuitBreakerAgentWorkerCommandState state;
    private AgentWorkerCommandContext context;
    private StatusUpdater statusUpdater;
    private boolean wasCircuitBreaked;

    public CircuitBreakerAgentWorkerCommand(
            AgentWorkerCommandContext context, CircuitBreakerAgentWorkerCommandState state) {
        this.context = context;
        this.statusUpdater = context;
        this.state = state;
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        if (isSnoopAndBackgroundRefresh()) {
            return AgentWorkerCommandResult.CONTINUE;
        }

        Credentials credentials = context.getRequest().getCredentials();
        Provider provider = context.getRequest().getProvider();

        wasCircuitBreaked = false;

        final CircuitBreakerConfiguration circuitBreakerConfiguration =
                context.getAgentsServiceConfiguration().getAggregationWorker().getCircuitBreaker();

        CircuitBreakerStatistics.CircuitBreakerStatus circuitBreakerStatus =
                state.getCircuitBreakerStatistics().get(provider).getStatus();

        if (Objects.equal(circuitBreakerConfiguration.getMode(), CircuitBreakerMode.DISABLED)) {
            return AgentWorkerCommandResult.CONTINUE;
        }

        if (circuitBreakerStatus.isCircuitBroken()) {
            state.setRateLimiterRate(
                    provider.getName(), circuitBreakerStatus.getRateLimitMultiplicationFactor());
            RateLimiter rateLimiter = state.getRateLimiter(provider.getName());

            // If we acquire a rate limiter the operation will continue as if the provider is not
            // circuit broken.
            if (!rateLimiter.tryAcquire()) {
                if (Objects.equal(
                        circuitBreakerConfiguration.getMode(), CircuitBreakerMode.ENABLED)) {
                    statusUpdater.updateStatusWithError(
                            CredentialsStatus.TEMPORARY_ERROR,
                            Catalog.format(
                                    context.getCatalog()
                                            .getString(
                                                    "We are currently having technical issues with {0}. Please try again later."),
                                    provider.getDisplayName()),
                            ConnectivityErrorFactory.providerError(
                                    ProviderErrors.PROVIDER_UNAVAILABLE));
                    wasCircuitBreaked = true;
                    logger.info("Operation aborted due to provider being circuit broken.");
                    return AgentWorkerCommandResult.ABORT;
                } else if (Objects.equal(
                        circuitBreakerConfiguration.getMode(), CircuitBreakerMode.TEST)) {
                    logger.info(
                            String.format(
                                    "[EVALUATION MODE] Provider: %s, Multiplication factor: %s",
                                    credentials.getProviderName(),
                                    circuitBreakerStatus.getRateLimitMultiplicationFactor()));
                }
            }
        }

        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    protected void doPostProcess() throws Exception {
        if (isSnoopAndBackgroundRefresh()) {
            return;
        }

        final CircuitBreakerStatistics circuitBreakerStatistics =
                state.getCircuitBreakerStatistics().get(context.getRequest().getProvider());

        // Register errors.

        if (!wasCircuitBreaked) {
            if (context.getRequest().getCredentials().getStatus()
                    == CredentialsStatus.TEMPORARY_ERROR) {
                circuitBreakerStatistics.registerError();
            } else {
                circuitBreakerStatistics.registerSuccess();
            }
        }
    }

    private boolean isSnoopAndBackgroundRefresh() {
        CredentialsRequest request = context.getRequest();
        return SNOOP_APP_ID.equals(context.getAppId()) && !request.isUserPresent();
    }
}
