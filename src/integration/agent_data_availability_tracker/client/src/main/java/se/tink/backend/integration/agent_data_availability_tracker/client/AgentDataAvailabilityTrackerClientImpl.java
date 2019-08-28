package se.tink.backend.integration.agent_data_availability_tracker.client;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.inject.Inject;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.netty.handler.ssl.SslContext;
import java.io.File;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.integration.agent_data_availability_tracker.api.AgentDataAvailabilityTrackerServiceGrpc;
import se.tink.backend.integration.agent_data_availability_tracker.api.TrackAccountRequest;
import se.tink.backend.integration.agent_data_availability_tracker.api.Void;
import se.tink.backend.integration.agent_data_availability_tracker.client.serialization.AccountTrackingSerializer;
import se.tink.backend.integration.agent_data_availability_tracker.client.serialization.LoanTrackingSerializer;
import se.tink.backend.integration.agent_data_availability_tracker.client.serialization.PortfolioTrackingSerializer;

public class AgentDataAvailabilityTrackerClientImpl implements AgentDataAvailabilityTrackerClient {

    private static final Logger log =
            LoggerFactory.getLogger(AgentDataAvailabilityTrackerClientImpl.class);

    private static final long QUEUE_POLL_WAIT_SECONDS = 2;

    private ManagedChannel channel;
    private AgentDataAvailabilityTrackerServiceGrpc.AgentDataAvailabilityTrackerServiceStub
            agentctServiceStub;

    private StreamObserver<TrackAccountRequest> requestStream;
    private final AccountDeque accountDeque;
    private final AbstractExecutionThreadService service;

    private boolean sendingData;

    private final String host;
    private final int port;

    private final Random random;
    private static final float TRACKING_FRACTION = 0.40f; // 20% of requests

    @Inject
    /** Construct client for accessing RouteGuide server at {@code host:port}. */
    public AgentDataAvailabilityTrackerClientImpl(
            AgentDataAvailabilityTrackerConfiguration configuration) {
        this(configuration.getHost(), configuration.getPort());
    }

    public AgentDataAvailabilityTrackerClientImpl(String host, int port) {
        this.host = host;
        this.port = port;
        this.accountDeque = new AccountDeque();
        this.random = new Random();
        this.service =
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
            final String agent, final Account account, final AccountFeatures features) {

        sendingData = sendingData();

        if (!sendingData) {
            return;
        }

        AccountTrackingSerializer serializer = new AccountTrackingSerializer(account);

        if (features.getPortfolios() != null) {
            features.getPortfolios().stream()
                    .map(PortfolioTrackingSerializer::new)
                    .forEach(e -> serializer.addChild("portfolios", e));
        }

        if (features.getLoans() != null) {
            features.getLoans().stream()
                    .map(LoanTrackingSerializer::new)
                    .forEach(e -> serializer.addChild("loans", e));
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

        log.debug(
                String.format(
                        "Adding request to queue: %d | Service running: %b",
                        accountDeque.size(), service.isRunning()));
        accountDeque.add(requestBuilder.build());
    }

    @Override
    public void endStreamBlocking() throws InterruptedException {
        requestStream.onCompleted();
        channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
    }

    private boolean sendingData() {
        return random.nextFloat() < TRACKING_FRACTION;
    }

    @Override
    public boolean sendingRealData() {
        return this.sendingData;
    }

    @Override
    public void start() throws Exception {

        SslContext sslContext =
                GrpcSslContexts.forClient()
                        .trustManager(new File("/etc/client-certificate/ca.crt"))
                        .build();

        log.debug(String.format("Opening connection: %s:%d", host, port));

        channel =
                NettyChannelBuilder.forAddress(host, port)
                        .useTransportSecurity()
                        .sslContext(sslContext)
                        .build();
        agentctServiceStub = AgentDataAvailabilityTrackerServiceGrpc.newStub(channel);

        beginStream();

        service.startAsync();
        service.awaitRunning(30, TimeUnit.SECONDS);
    }

    @Override
    public void stop() throws Exception {
        log.debug("Received signal to stop.");
        service.stopAsync();
        service.awaitTerminated(60, TimeUnit.SECONDS);
        endStreamBlocking();
    }
}
