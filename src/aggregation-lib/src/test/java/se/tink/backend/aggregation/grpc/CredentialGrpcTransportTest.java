package se.tink.backend.aggregation.grpc;

import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.tink.aggregation.grpc.CreateCredentialRequest;
import se.tink.aggregation.grpc.Credential;
import se.tink.aggregation.grpc.CredentialServiceGrpc;
import static org.junit.Assert.assertEquals;

public class CredentialGrpcTransportTest {

    private Server server;
    private CredentialServiceGrpc.CredentialServiceBlockingStub client;

    @Before
    public void setUp() throws IOException {
        server = InProcessServerBuilder.forName("credential-grpc")
                .addService(new CredentialGrpcTransport())
                .directExecutor()
                .build();
        server.start();
        client = CredentialServiceGrpc.newBlockingStub(InProcessChannelBuilder.forName("credential-grpc")
                .directExecutor()
                .build());
    }

    @After
    public void tearDown() {
        server.shutdown();
    }

    @Test
    public void returnCreatedCredential() {
        CreateCredentialRequest.Builder credentialRequest = CreateCredentialRequest.newBuilder()
                .setCredential(Credential.newBuilder().setId("credentialId").build());
        assertEquals(
                "credentialId",
                client.createCredential(credentialRequest.build()).getCredential().getId());
    }
}
