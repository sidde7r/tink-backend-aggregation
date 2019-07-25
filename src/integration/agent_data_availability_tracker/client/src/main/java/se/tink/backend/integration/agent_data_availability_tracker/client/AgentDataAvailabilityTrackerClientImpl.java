package se.tink.backend.integration.agent_data_availability_tracker.client;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.netty.handler.ssl.SslContext;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.integration.agent_data_availability_tracker.api.AgentDataAvailabilityTrackerServiceGrpc;
import se.tink.backend.integration.agent_data_availability_tracker.api.TrackAccountRequest;
import se.tink.backend.integration.agent_data_availability_tracker.api.Void;
import se.tink.backend.integration.agent_data_availability_tracker.client.serialization.AccountTrackingSerializer;
import se.tink.backend.integration.agent_data_availability_tracker.client.serialization.PortfolioTrackingSerializer;

public class AgentDataAvailabilityTrackerClientImpl implements AgentDataAvailabilityTrackerClient {

    private static final Logger log =
            LoggerFactory.getLogger(AgentDataAvailabilityTrackerClientImpl.class);

    private final ManagedChannel channel;
    private final AgentDataAvailabilityTrackerServiceGrpc.AgentDataAvailabilityTrackerServiceStub
            agentctServiceStub;

    private StreamObserver<TrackAccountRequest> requestStream;

    private CountDownLatch latch;

    /** Construct client for accessing RouteGuide server at {@code host:port}. */
    public AgentDataAvailabilityTrackerClientImpl(String host, int port) throws SSLException {
        this(NettyChannelBuilder.forAddress(host, port));
    }

    /** Construct client for accessing RouteGuide server using the existing channel. */
    public AgentDataAvailabilityTrackerClientImpl(NettyChannelBuilder channelBuilder)
            throws SSLException {

        SslContext sslContext;

        sslContext =
                GrpcSslContexts.forClient()
                        .trustManager(new File("/etc/client-certificate/ca.crt"))
                        .build();

        channel = channelBuilder.useTransportSecurity().sslContext(sslContext).build();
        agentctServiceStub = AgentDataAvailabilityTrackerServiceGrpc.newStub(channel);
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

            TrackAccountRequest.Builder requestBuilder =
                    TrackAccountRequest.newBuilder().setAgent(agent);

            // TODO: Unwrapped serialization such that builder.setAll can be used instead of loop
            serializer
                    .buildList()
                    .forEach(
                            entry ->
                                    requestBuilder
                                            .addFieldName(entry.getName())
                                            .addFieldValue(entry.getValue()));

            requestStream.onNext(requestBuilder.build());

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

    public void endStreamBlocking() throws InterruptedException {
        requestStream.onCompleted();

        try {
            latch.await(500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.warn("Waiting for tracking client to catch up for more than 500ms");
        }

        latch.await(2, TimeUnit.SECONDS);

        channel.shutdown().awaitTermination(1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean isMockClient() {
        return false;
    }
}
