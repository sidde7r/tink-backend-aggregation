package se.tink.backend.integration.agent_data_availability_tracker.client;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.inject.Inject;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.netty.handler.ssl.SslContext;
import java.io.File;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.IdentityData;
import se.tink.backend.integration.agent_data_availability_tracker.api.AgentDataAvailabilityTrackerServiceGrpc;
import se.tink.backend.integration.agent_data_availability_tracker.api.TrackAccountRequest;
import se.tink.backend.integration.agent_data_availability_tracker.api.Void;
import se.tink.backend.integration.agent_data_availability_tracker.client.configuration.AgentDataAvailabilityTrackerConfiguration;
import se.tink.backend.integration.agent_data_availability_tracker.client.serialization.AccountTrackingSerializer;
import se.tink.backend.integration.agent_data_availability_tracker.client.serialization.IdentityDataSerializer;
import se.tink.backend.integration.agent_data_availability_tracker.client.serialization.LoanTrackingSerializer;
import se.tink.backend.integration.agent_data_availability_tracker.client.serialization.PortfolioTrackingSerializer;

public class AgentDataAvailabilityTrackerClientImpl implements AgentDataAvailabilityTrackerClient {

    private static final Logger log =
            LoggerFactory.getLogger(AgentDataAvailabilityTrackerClient.class);

    private static final long QUEUE_POLL_WAIT_SECONDS = 2;

    private ManagedChannel channel;
    private AgentDataAvailabilityTrackerServiceGrpc.AgentDataAvailabilityTrackerServiceStub
            agentctServiceStub;

    private StreamObserver<TrackAccountRequest> requestStream;
    private final AccountDeque accountDeque;
    private final AbstractExecutionThreadService service;

    private final String host;
    private final int port;
    private final String caPath;

    /** Construct client for accessing RouteGuide server at {@code host:port}. */
    @Inject
    private AgentDataAvailabilityTrackerClientImpl(
            AgentDataAvailabilityTrackerConfiguration configuration) {
        this(configuration.getHost(), configuration.getPort(), configuration.getCaPath());
    }

    public AgentDataAvailabilityTrackerClientImpl(String host, int port) {
        this(host, port, null);
    }

    public AgentDataAvailabilityTrackerClientImpl(String host, int port, String caPath) {
        this.host = host;
        this.port = port;
        this.caPath = caPath;
        this.accountDeque = new AccountDeque();
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

    public AccountTrackingSerializer serializeAccount(
            final Account account, final AccountFeatures features) {
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

        return serializer;
    }

    public void sendAccount(
            final String agent,
            final String provider,
            final String market,
            final Account account,
            final AccountFeatures features) {

        TrackAccountRequest.Builder requestBuilder =
                TrackAccountRequest.newBuilder()
                        .setAgent(agent)
                        .setProvider(provider)
                        .setMarket(market);

        AccountTrackingSerializer serializer = serializeAccount(account, features);

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

    public IdentityDataSerializer serializeIdentityData(final IdentityData identityData) {
        return new IdentityDataSerializer(identityData);
    }

    public void sendIdentityData(
            final String agent,
            final String provider,
            final String market,
            final IdentityData identityData) {

        TrackAccountRequest.Builder requestBuilder =
                TrackAccountRequest.newBuilder()
                        .setAgent(agent)
                        .setProvider(provider)
                        .setMarket(market);

        IdentityDataSerializer serializer = serializeIdentityData(identityData);

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

        NettyChannelBuilder channelBuilder = NettyChannelBuilder.forAddress(host, port);

        if (Strings.isNullOrEmpty(caPath)) {
            channelBuilder.usePlaintext();
        } else {
            SslContext sslContext =
                    GrpcSslContexts.forClient().trustManager(new File(caPath)).build();

            channelBuilder.useTransportSecurity().sslContext(sslContext).build();
        }

        channel = channelBuilder.build();

        log.debug(String.format("Opening connection: %s:%d", host, port));
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
