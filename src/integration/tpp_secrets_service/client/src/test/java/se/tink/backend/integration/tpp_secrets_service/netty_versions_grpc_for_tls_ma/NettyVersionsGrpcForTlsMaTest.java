package se.tink.backend.integration.tpp_secrets_service.netty_versions_grpc_for_tls_ma;

import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.salesforce.grpc.testing.contrib.NettyGrpcServerRule;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.protobuf.SimpleRequest;
import io.grpc.testing.protobuf.SimpleResponse;
import io.grpc.testing.protobuf.SimpleServiceGrpc;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.LoggerFactory;

// Test that reproduces the errors from this incident
// Slack: https://tink.slack.com/archives/CG4ECH2AF/p1569328523082200
// Google doc: https://docs.google.com/document/d/1Akrt0W75PGR1gztI81fx4_4qY18EcguGhH-FK6zFYJs
//
// AFAIK the test detects a mismatch between the version of netty-tcnative-boringssl-static and
// netty-handler. The version of grpc-netty should in theory also be aligned with the previous two
// according to this table https://github.com/grpc/grpc-java/blob/v1.23.x/SECURITY.md#netty but at
// the time of the incident their versions where:
//      grpc-netty: 1.24
//      netty-handler: 4.1.22.Final
//      netty-tcnative-boringssl-static: 2.0.7.Final
// and the incident happened when netty-tcnative-boringssl-static was upgraded to 2.0.17.Final so
// the dependency seems to be more loosely coupled between grpc-netty and the other two. Anyway they
// should be aligned according to the table linked above to avoid problems.
public class NettyVersionsGrpcForTlsMaTest {
    private final Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    private final Level originalLevel = rootLogger.getLevel();

    @Rule public NettyGrpcServerRule grpcServerRule;

    public NettyVersionsGrpcForTlsMaTest() {
        // The default DEBUG level prints a lot of unuseful information that might confuse the user
        // in the event of a failed test case.
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        Level originalLevel = rootLogger.getLevel();
        rootLogger.setLevel(Level.INFO);

        try {
            SelfSignedCertificate ssc = new SelfSignedCertificate("localhost");

            // Important to add trustManager and required client authentication since the problem
            // doesn't happen when there is no client authentication.
            SslContext serverSslContext =
                    GrpcSslContexts.forServer(ssc.certificate(), ssc.privateKey())
                            .trustManager(ssc.cert())
                            .clientAuth(ClientAuth.REQUIRE)
                            .build();
            SslContext clientSslContext =
                    GrpcSslContexts.forClient()
                            .trustManager(ssc.cert())
                            .keyManager(ssc.certificate(), ssc.privateKey())
                            .build();

            grpcServerRule =
                    new NettyGrpcServerRule()
                            .configureServerBuilder(
                                    serverBuilder -> serverBuilder.sslContext(serverSslContext))
                            .configureChannelBuilder(
                                    channelBuilder ->
                                            channelBuilder
                                                    .useTransportSecurity()
                                                    .sslContext(clientSslContext));
        } catch (CertificateException | SSLException e) {
            rootLogger.setLevel(originalLevel);
            Assert.fail(
                    "Unexpected Exception while setting up NettyGrpcServerRule. Exception: " + e);
        }
    }

    private SimpleServiceGrpc.SimpleServiceImplBase simpleServiceImpl =
            mock(
                    SimpleServiceGrpc.SimpleServiceImplBase.class,
                    delegatesTo(
                            new SimpleServiceGrpc.SimpleServiceImplBase() {
                                @Override
                                public void unaryRpc(
                                        SimpleRequest request,
                                        StreamObserver<SimpleResponse> responseObserver) {
                                    responseObserver.onNext(SimpleResponse.getDefaultInstance());
                                    responseObserver.onCompleted();
                                }
                            }));

    private SimpleServiceGrpc.SimpleServiceBlockingStub simpleServiceClient;

    @Before
    public void setUp() {
        grpcServerRule.getServiceRegistry().addService(simpleServiceImpl);
        simpleServiceClient = SimpleServiceGrpc.newBlockingStub(grpcServerRule.getChannel());
    }

    @After
    public void tearDown() {
        rootLogger.setLevel(originalLevel);
        try {
            grpcServerRule.getServer().shutdown().awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.out.println("Problems shutting down the server.");
        }
    }

    @Test
    public void test_UnaryRpc() {
        try {
            simpleServiceClient.unaryRpc(SimpleRequest.getDefaultInstance());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(
                    String.format(
                            "Possible version mismatch between Netty dependencies, have you changed any of the following recently?"
                                    + "\n\tgrpc-netty"
                                    + "\n\tnetty-handler"
                                    + "\n\tnetty-tcnative-boringssl-static"
                                    + "\nIf so check the compatibility table at https://github.com/grpc/grpc-java/blob/v1.23.x/SECURITY.md#netty to make sure you are using the right combination of versions."
                                    + "\n\n%s",
                            e.getMessage()));
        }
    }
}
