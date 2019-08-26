package se.tink.backend.integration.agent_data_availability_tracker.client;

import io.dropwizard.lifecycle.Managed;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.netty.handler.ssl.SslContext;
import java.io.File;
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

public class AgentDataAvailabilityTrackerClientImpl
        implements AgentDataAvailabilityTrackerClient, Managed {

    private static final Logger log =
            LoggerFactory.getLogger(AgentDataAvailabilityTrackerClientImpl.class);

    private ManagedChannel channel;
    private AgentDataAvailabilityTrackerServiceGrpc.AgentDataAvailabilityTrackerServiceStub
            agentctServiceStub;

    private StreamObserver<TrackAccountRequest> requestStream;
    private final NettyChannelBuilder channelBuilder;
    private final AccountDeque accountDeque;

    /** Construct client for accessing RouteGuide server at {@code host:port}. */
    public AgentDataAvailabilityTrackerClientImpl(String host, int port) throws SSLException {
        this(NettyChannelBuilder.forAddress(host, port));
    }

    /** Construct client for accessing RouteGuide server using the existing channel. */
    public AgentDataAvailabilityTrackerClientImpl(NettyChannelBuilder channelBuilder)
            throws SSLException {
        this.channelBuilder = channelBuilder;
        this.accountDeque = new AccountDeque();
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

            accountDeque.add(requestBuilder.build());

            requestStream.onNext(accountDeque.pop());

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

    @Override
    public void endStreamBlocking() throws InterruptedException {
        requestStream.onCompleted();
        channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
    }

    @Override
    public boolean isMockClient() {
        return false;
    }

    @Override
    public void start() throws Exception {

        SslContext sslContext =
                GrpcSslContexts.forClient()
                        .trustManager(new File("/etc/client-certificate/ca.crt"))
                        .build();

        channel = channelBuilder.useTransportSecurity().sslContext(sslContext).build();
        agentctServiceStub = AgentDataAvailabilityTrackerServiceGrpc.newStub(channel);

        beginStream();
    }

    @Override
    public void stop() throws Exception {
        endStreamBlocking();
    }
}
