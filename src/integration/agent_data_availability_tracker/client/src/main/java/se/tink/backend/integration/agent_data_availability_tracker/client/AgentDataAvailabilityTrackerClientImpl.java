package se.tink.backend.integration.agent_data_availability_tracker.client;

import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.integration.agent_data_availability_tracker.api.AccountField;
import se.tink.backend.integration.agent_data_availability_tracker.api.AgentDataAvailabilityTrackerServiceGrpc;
import se.tink.backend.integration.agent_data_availability_tracker.api.TrackAccountRequest;
import se.tink.backend.integration.agent_data_availability_tracker.api.Void;
import se.tink.backend.integration.agent_data_availability_tracker.client.serialization.AccountTrackingSerializer;
import se.tink.backend.integration.agent_data_availability_tracker.client.serialization.PortfolioTrackingSerializer;

public class AgentDataAvailabilityTrackerClientImpl implements AgentDataAvailabilityTrackerClient {

    private static final Logger log =
            LoggerFactory.getLogger(AgentDataAvailabilityTrackerClientImpl.class);

    private StreamObserver<TrackAccountRequest> requestStream;

    private CountDownLatch latch;
    private AgentDataAvailabilityTrackerServiceGrpc.AgentDataAvailabilityTrackerServiceStub
            agentctServiceStub;

    /** Construct client for accessing RouteGuide server at {@code host:port}. */
    public AgentDataAvailabilityTrackerClientImpl(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext());
    }

    /** Construct client for accessing RouteGuide server using the existing channel. */
    public AgentDataAvailabilityTrackerClientImpl(ManagedChannelBuilder<?> channelBuilder) {
        agentctServiceStub =
                AgentDataAvailabilityTrackerServiceGrpc.newStub(channelBuilder.build());
    }

    public void beginStream() {

        log.debug("Open Tracking Stream");

        latch = new CountDownLatch(1);

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
                        latch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        log.debug("Tracking request batch done.");
                        latch.countDown();
                    }
                };

        requestStream = agentctServiceStub.trackAccount(responseObserver);
    }

    public void sendAccount(
            final String agent, final Account account, final AccountFeatures features) {

        try {

            AccountTrackingSerializer serializer = new AccountTrackingSerializer(account);

            if (features.getPortfolios() != null) {
                features.getPortfolios().stream()
                        .map(PortfolioTrackingSerializer::new)
                        .forEach(e -> serializer.addChild("portfolios", e));
            }

            TrackAccountRequest request =
                    TrackAccountRequest.newBuilder()
                            .setAgent(agent)
                            .addAllField(
                                    serializer.buildList().stream()
                                            .map(
                                                    field ->
                                                            AccountField.newBuilder()
                                                                    .setFieldName(field.getName())
                                                                    .setFieldValue(field.getValue())
                                                                    .build())
                                            .collect(Collectors.toList()))
                            .build();

            requestStream.onNext(request);

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

    public void endStreamBlocking() {
        requestStream.onCompleted();

        log.warn("Waiting for tracking client to catch up...");
        try {
            latch.await(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
