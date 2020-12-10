package se.tink.backend.integration.agent_data_availability_tracker.client;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.inject.Inject;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.integration.agent_data_availability_tracker.api.AgentDataAvailabilityTrackerServiceGrpc;
import se.tink.backend.integration.agent_data_availability_tracker.api.TrackAccountRequest;
import se.tink.backend.integration.agent_data_availability_tracker.api.Void;
import se.tink.backend.integration.agent_data_availability_tracker.common.client.AccountDeque;
import se.tink.backend.integration.agent_data_availability_tracker.common.client.AgentDataAvailabilityTrackerClient;
import se.tink.backend.integration.agent_data_availability_tracker.common.serialization.TrackingMapSerializer;
import se.tink.libraries.dropwizard_lifecycle.ManagedSafeStop;

public class AsAgentDataAvailabilityTrackerClientImpl extends ManagedSafeStop
        implements AgentDataAvailabilityTrackerClient {

    private static final Logger log =
            LoggerFactory.getLogger(AgentDataAvailabilityTrackerClient.class);

    private static final long QUEUE_POLL_WAIT_SECONDS = 2;

    private ManagedChannel channel;
    private AgentDataAvailabilityTrackerServiceGrpc.AgentDataAvailabilityTrackerServiceStub
            agentctServiceStub;

    private StreamObserver<TrackAccountRequest> requestStream;
    private final AccountDeque accountDeque;
    private final AbstractExecutionThreadService service;

    /** Construct client for accessing RouteGuide server at {@code host:port}. */
    @Inject
    private AsAgentDataAvailabilityTrackerClientImpl(final ManagedChannel channel) {
        this.channel = channel;
        accountDeque = new AccountDeque();
        service =
                new AbstractExecutionThreadService() {
                    @Override
                    protected void run() throws Exception {
                        sendAccounts();
                    }
                };
    }

    @Override
    public void beginStream() {

        log.debug("Open Tracking Stream");

        StreamObserver<Void> responseObserver =
                new StreamObserver<Void>() {
                    @Override
                    public void onNext(Void aVoid) {
                        log.debug("Tracking request OK.");
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        log.warn(
                                String.format("Tracking error: %s", throwable.getMessage()),
                                throwable);
                    }

                    @Override
                    public void onCompleted() {
                        log.debug("Tracking request batch done.");
                    }
                };

        requestStream = agentctServiceStub.trackAccount(responseObserver);
    }

    private void sendAccounts() throws InterruptedException {

        while (service.isRunning()) {
            try {

                TrackAccountRequest request =
                        accountDeque.poll(QUEUE_POLL_WAIT_SECONDS, TimeUnit.SECONDS);

                // If poll timed out request will be null, in this case the loop will check if we
                // are still running and terminate or try polling again.
                if (request != null) {

                    requestStream.onNext(request);
                }

            } catch (StatusRuntimeException e) {

                log.warn(
                        String.format(
                                "Aborting tracking attempt. Capability Tracking service code: %s",
                                e.getStatus()),
                        e);
                requestStream.onError(e);
            } catch (Exception e) {

                log.warn(String.format("Tracking failed with exception: %s", e.getMessage()), e);
                requestStream.onError(e);
            }
        }
    }

    public void sendAccount(
            final String agent,
            final String provider,
            final String market,
            final TrackingMapSerializer serializer) {

        TrackAccountRequest.Builder requestBuilder =
                TrackAccountRequest.newBuilder()
                        .setAgent(agent)
                        .setProvider(provider)
                        .setMarket(market);

        // TODO: Unwrapped serialization such that builder.setAll can be used instead of loop
        serializer
                .buildList()
                .forEach(
                        entry ->
                                requestBuilder
                                        .addFieldName(entry.getName())
                                        .addFieldValue(entry.getValue()));

        accountDeque.add(requestBuilder.build());
    }

    public void sendIdentityData(
            final String agent,
            final String provider,
            final String market,
            final TrackingMapSerializer identityDataSerializer) {

        TrackAccountRequest.Builder requestBuilder =
                TrackAccountRequest.newBuilder()
                        .setAgent(agent)
                        .setProvider(provider)
                        .setMarket(market);

        identityDataSerializer
                .buildList()
                .forEach(
                        entry ->
                                requestBuilder
                                        .addFieldName(entry.getName())
                                        .addFieldValue(entry.getValue()));

        accountDeque.add(requestBuilder.build());
    }

    @Override
    public void endStreamBlocking() throws InterruptedException {
        requestStream.onCompleted();
        channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
    }

    @Override
    public boolean sendingRealData() {
        return true;
    }

    @Override
    public void start() throws Exception {
        agentctServiceStub = AgentDataAvailabilityTrackerServiceGrpc.newStub(channel);

        beginStream();
        service.startAsync();
        service.awaitRunning(30, TimeUnit.SECONDS);
    }

    @Override
    public void doStop() throws Exception {
        log.debug("Received signal to stop.");
        service.stopAsync();
        service.awaitTerminated(60, TimeUnit.SECONDS);
        endStreamBlocking();
    }
}
