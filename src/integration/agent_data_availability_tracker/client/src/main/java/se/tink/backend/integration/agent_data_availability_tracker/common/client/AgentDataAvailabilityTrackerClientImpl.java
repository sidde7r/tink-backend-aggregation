package se.tink.backend.integration.agent_data_availability_tracker.common.client;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.integration.agent_data_availability_tracker.api.AgentDataAvailabilityTrackerServiceGrpc;
import se.tink.backend.integration.agent_data_availability_tracker.api.TrackAccountRequest;
import se.tink.backend.integration.agent_data_availability_tracker.api.Void;
import se.tink.backend.integration.agent_data_availability_tracker.common.serialization.TrackingMapSerializer;

public class AgentDataAvailabilityTrackerClientImpl implements AgentDataAvailabilityTrackerClient {

    private static final Logger log =
            LoggerFactory.getLogger(AgentDataAvailabilityTrackerClientImpl.class);
    private static final long QUEUE_POLL_WAIT_SECONDS = 2;
    private final AccountDeque accountDeque;
    private final AbstractExecutionThreadService service;
    private ManagedChannel channel;
    private StreamObserver<TrackAccountRequest> requestStream;
    private AgentDataAvailabilityTrackerServiceGrpc.AgentDataAvailabilityTrackerServiceStub
            agentctServiceStub;

    public AgentDataAvailabilityTrackerClientImpl(final ManagedChannel channel) {
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

    @Override
    public void sendAccount(
            String agent, String provider, String market, TrackingMapSerializer serializer) {
        TrackAccountRequest.Builder requestBuilder =
                TrackAccountRequest.newBuilder()
                        .setAgent(agent)
                        .setProvider(provider)
                        .setMarket(market);

        serializer
                .buildList()
                .forEach(
                        entry ->
                                requestBuilder
                                        .addFieldName(entry.getName())
                                        .addFieldValue(entry.getValue()));

        accountDeque.add(requestBuilder.build());
    }

    @Override
    public void sendIdentityData(
            String agent,
            String provider,
            String market,
            TrackingMapSerializer identityDataSerializer) {
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

    public void start() throws TimeoutException {
        agentctServiceStub = AgentDataAvailabilityTrackerServiceGrpc.newStub(channel);
        beginStream();
        service.startAsync();
        service.awaitRunning(30, TimeUnit.SECONDS);
    }

    public void stop() throws TimeoutException, InterruptedException {
        log.debug("Received signal to stop.");
        service.stopAsync();
        service.awaitTerminated(60, TimeUnit.SECONDS);
        endStreamBlocking();
    }
}
