package se.tink.oauth;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.dropwizard.lifecycle.Managed;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import se.tink.libraries.endpoints.EndpointConfiguration;
import se.tink.backend.guice.annotations.OAuthConfiguration;
import se.tink.libraries.log.LogUtils;
import se.tink.oauth.grpc.Authorization;
import se.tink.oauth.grpc.AuthorizationsRequest;
import se.tink.oauth.grpc.AuthorizationsResponse;
import se.tink.oauth.grpc.Client;
import se.tink.oauth.grpc.ClientRequest;
import se.tink.oauth.grpc.ClientsRequest;
import se.tink.oauth.grpc.ClientsResponse;
import se.tink.oauth.grpc.OAuthGrpc;
import se.tink.oauth.grpc.PingRequest;

public class OAuthGrpcClient implements Managed {
    private ManagedChannelBuilder<?> channelBuilder;
    private OAuthGrpc.OAuthBlockingStub blockingStub;
    private ManagedChannel channel;
    private static final LogUtils log = new LogUtils(OAuthGrpcClient.class);

    @Inject
    public OAuthGrpcClient(@OAuthConfiguration EndpointConfiguration endpoint) throws MalformedURLException {
        URL url = new URL(endpoint.getUrl());
        channelBuilder = ManagedChannelBuilder.forAddress(url.getHost(), url.getPort()).usePlaintext(true);
    }

    public List<Client> getClients() {
        try {
            ClientsResponse response = blockingStub.getClients(ClientsRequest.newBuilder().build());
            return response.getClientsList();
        } catch (StatusRuntimeException e) {
            logPotentialErrors(e, "Could not fetch clients");
            return Lists.newArrayList();
        }
    }

    public Optional<Client> getClient(String id) {
        try {
            return Optional.of(blockingStub.getClient(ClientRequest.newBuilder().setId(id).build()));
        } catch (StatusRuntimeException e) {
            logPotentialErrors(e, "Could not fetch client with id: " + id);
            return Optional.empty();
        }
    }

    public List<Authorization> getAuthorizations(String clientId) {
        try {
            AuthorizationsRequest request = AuthorizationsRequest.newBuilder().setClientId(clientId).build();
            AuthorizationsResponse response = blockingStub.getAuthorizations(request);
            return response.getAuthorizationsList();
        } catch (StatusRuntimeException e) {
            logPotentialErrors(e, "Could not fetch authorizations with clientId: " + clientId);
            return Lists.newArrayList();
        }
    }

    @PostConstruct
    public void start() {
        log.info("Starting OAuth gRPC client");
        channel = channelBuilder.build();
        blockingStub = OAuthGrpc.newBlockingStub(channel);
        dryRun();
    }

    @Override
    public void stop() throws InterruptedException {
        log.info("Stopping OAuth gRPC client");
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    private void dryRun() {
        blockingStub.ping(PingRequest.newBuilder().build());
    }

    /**
     * Some statuses should always be considered as errors, such as connection issues, while
     * for other statuses it depends on the usage.
     */
    private void logPotentialErrors(StatusRuntimeException e, String message) {
        switch (e.getStatus().getCode()) {
        case NOT_FOUND:
        case ALREADY_EXISTS:
        case OK:
            return;
        default:
            log.error(message, e);
        }
    }
}
