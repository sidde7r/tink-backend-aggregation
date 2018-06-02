package se.tink.aggregation.client;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import org.apache.curator.x.discovery.ServiceCache;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceType;
import org.apache.curator.x.discovery.UriSpec;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.aggregation.grpc.CreateCredentialRequest;
import se.tink.aggregation.grpc.CreateCredentialResponse;
import se.tink.aggregation.grpc.Credential;
import se.tink.aggregation.grpc.CredentialServiceGrpc;
import se.tink.backend.core.Credentials;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CredentialGrpcClientIntegrationTest {

    @Mock ServiceCache<String> serviceCache;

    CredentialGrpcClient client;
    CredentialGrpcRouter router;

    @Before
    public void setUp() throws Exception {
        router = new CredentialGrpcRouter(serviceCache);
        client = new CredentialGrpcClient(router);
    }

    @Test
    public void create() throws IOException {
        Server server1 = startServer();
        Server server2 = startServer();
        when(serviceCache.getInstances()).thenReturn(asList(
                createInstance(server1.getPort()),
                createInstance(server2.getPort())
        ));
        router.cacheChanged();

        Credentials credentials = new Credentials();
        credentials.setId("credentialId");
        assertEquals("credentialId", client.create(credentials).getCredential().getId());

        server1.shutdown();
        server2.shutdown();
    }

    @Test
    public void retryCreateWhenServerUnavailable() throws IOException {
        AtomicBoolean firstCall = new AtomicBoolean(true);
        Server server = startServer((request, responseObserver) -> {
            if (firstCall.get()) {
                firstCall.set(false);
                responseObserver.onError(Status.UNAVAILABLE.asException());
            } else {
                responseObserver.onNext(newCreateCredentialResponse(request));
            }
        });
        when(serviceCache.getInstances()).thenReturn(singletonList(createInstance(server.getPort())));
        router.cacheChanged();

        Credentials credentials = new Credentials();
        credentials.setId("credentialId");
        assertEquals("credentialId", client.create(credentials).getCredential().getId());

        server.shutdown();
    }

    @Test(expected = GrpcClientException.class)
    public void createWhenAllServersDown() throws IOException {
        Server brokenServer = startBrokenServer();
        when(serviceCache.getInstances()).thenReturn(singletonList(createInstance(brokenServer.getPort())));
        router.cacheChanged();

        client.create(new Credentials());

        brokenServer.shutdown();
    }

    static Server startServer() throws IOException {
        return startServer((request, responseObserver) -> responseObserver.onNext(newCreateCredentialResponse(request)));
    }

    static Server startBrokenServer() throws IOException {
        return startServer((request, responseObserver) -> responseObserver.onError(Status.UNAVAILABLE.asException()));
    }

    static Server startServer(BiConsumer<CreateCredentialRequest, StreamObserver<CreateCredentialResponse>> responseCreator)
            throws IOException {
        return ServerBuilder.forPort(0)
                .addService(new CredentialServiceGrpc.CredentialServiceImplBase() {
                    @Override public void createCredential(CreateCredentialRequest request,
                            StreamObserver<CreateCredentialResponse> responseObserver) {
                        responseCreator.accept(request, responseObserver);
                        responseObserver.onCompleted();
                    }
                })
                .build()
                .start();
    }

    static CreateCredentialResponse newCreateCredentialResponse(CreateCredentialRequest request) {
        return CreateCredentialResponse.newBuilder().setCredential(
                Credential.newBuilder().setId(request.getCredential().getId()).build())
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    static ServiceInstance<String> createInstance(int port) {
        Integer sslPort = null;
        int registrationTIme = 0;
        return new ServiceInstance<>("aggregationServiceName", "instanceId-" + port,
                "localhost", port, sslPort, "payload", registrationTIme, ServiceType.DYNAMIC, new UriSpec());
    }

}
