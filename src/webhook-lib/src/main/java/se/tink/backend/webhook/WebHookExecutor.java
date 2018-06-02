package se.tink.backend.webhook;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.StopStrategy;
import com.github.rholder.retry.WaitStrategy;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Range;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import javax.ws.rs.core.MediaType;
import se.tink.backend.common.retry.RetryerBuilder;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.webhook.rpc.WebHookRequest;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

public class WebHookExecutor {

    private static final LogUtils log = new LogUtils(WebHookExecutor.class);
    private static final Range<Integer> SUCCESSFUL_RESPONSE_RANGE = Range.closed(200, 299);

    private static final MetricId WEBHOOK = MetricId.newId("webhook_executor");
    private static final MetricId.MetricLabels EXECUTED_LABEL= new MetricId.MetricLabels()
            .add("event", "executed");
    private static final MetricId.MetricLabels FAILED_LABEL = new MetricId.MetricLabels()
            .add("event", "failed");

    private final Counter executedWebhookCounter;
    private final Counter failedWebhookCounter;

    private static final Predicate<ClientResponse> SUCCESSFUL_RESPONSE = response -> response != null &&
            (Objects.equals(response.getStatus(), ClientResponse.Status.OK.getStatusCode())
                    || SUCCESSFUL_RESPONSE_RANGE.contains(response.getStatus()));

    private final Client client;
    private final Retryer<ClientResponse> retryer;

    public WebHookExecutor(final Client client, StopStrategy stopStrategy, WaitStrategy waitStrategy,
            MetricRegistry metricRegistry) {

        this.client = client;
        this.client.setFollowRedirects(false); // make sure

        executedWebhookCounter = metricRegistry.meter(WEBHOOK.label(EXECUTED_LABEL));
        failedWebhookCounter = metricRegistry.meter(WEBHOOK.label(FAILED_LABEL));

        this.retryer = RetryerBuilder.<ClientResponse>newBuilder(log, "webhook-execution")
                .retryIfException()
                .retryIfResult(Predicates.not(SUCCESSFUL_RESPONSE))
                .withStopStrategy(stopStrategy)
                .withWaitStrategy(waitStrategy)
                .build();
    }

    public void execute(final WebHookRequest request) {
        ClientResponse clientResponse = null;
        
        try {
            log.debug("Executing webhook: " + request.getWebhook().getUrl());
            clientResponse = retryer.call(() -> client.resource(request.getWebhook().getUrl())
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .post(ClientResponse.class, request));

            executedWebhookCounter.inc();
        } catch (ExecutionException e) {
            log.warn("Could not execute webhook.", e);
            failedWebhookCounter.inc();
        } catch (RetryException e) {
            log.warn("Could not execute webhook. Tried too many times but recipient did not send an OK answer.", e);
            failedWebhookCounter.inc();
        } catch (ClientHandlerException e) {
            log.warn("Could not close Client Response. Webhook might still have been executed.", e);
            failedWebhookCounter.inc();
        } finally {
            if (clientResponse != null) {
                clientResponse.close();
            }
        }
    }
}
