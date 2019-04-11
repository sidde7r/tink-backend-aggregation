package se.tink.backend.integration.gprcserver;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.netty.NettyServerBuilder;
import java.io.*;
import java.net.SocketAddress;
import java.security.PrivateKey;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.crypto.util.PrivateKeyInfoFactory;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;

public class GrpcServer {
    private Server server;
    private NettyServerBuilder serverBuilder;
    private static final Logger logger = LogManager.getLogger(GrpcServer.class);

    @Inject
    GrpcServer(
            @Named("grpcServices") Set<BindableService> services,
            @Named("grpcInterceptors") Set<ServerInterceptor> interceptors,
            @Named("grpcSocket") SocketAddress listenAddress) {
        serverBuilder = NettyServerBuilder.forAddress(listenAddress);

        serverBuilder.executor(createExecutor());
        services.stream()
                .map(s -> ServerInterceptors.intercept(s, Lists.newArrayList(interceptors)))
                .forEach(serverBuilder::addService);
    }

    public void useTransportSecurity(File certChain, File privateKey) throws IOException {
        serverBuilder.useTransportSecurity(new FileInputStream(certChain), ensurePkcs8(privateKey));
    }

    /**
     * Converts private keys in format PKCS#1 and PKCS#8 to PKCS#8
     *
     * @param privateKeyFile
     * @return
     * @throws IOException
     */
    public static InputStream ensurePkcs8(final File privateKeyFile) throws IOException {
        PemReader pemReader = new PemReader(new FileReader(privateKeyFile));
        PemObject privateKeyObject = pemReader.readPemObject();

        RSAPrivateCrtKeyParameters privateKeyParameter;

        if (privateKeyObject.getType().endsWith("RSA PRIVATE KEY")) {
            // PKCS#1 key
            RSAPrivateKey rsa = RSAPrivateKey.getInstance(privateKeyObject.getContent());
            privateKeyParameter =
                    new RSAPrivateCrtKeyParameters(
                            rsa.getModulus(),
                            rsa.getPublicExponent(),
                            rsa.getPrivateExponent(),
                            rsa.getPrime1(),
                            rsa.getPrime2(),
                            rsa.getExponent1(),
                            rsa.getExponent2(),
                            rsa.getCoefficient());
        } else if (privateKeyObject.getType().endsWith("PRIVATE KEY")) {
            // PKCS#8 key
            privateKeyParameter =
                    (RSAPrivateCrtKeyParameters)
                            PrivateKeyFactory.createKey(privateKeyObject.getContent());
        } else {
            throw new RuntimeException("Unsupported key type: " + privateKeyObject.getType());
        }

        PrivateKey privateKey =
                new JcaPEMKeyConverter()
                        .getPrivateKey(
                                PrivateKeyInfoFactory.createPrivateKeyInfo(privateKeyParameter));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        PemWriter pemWriter = new PemWriter(new OutputStreamWriter(outputStream));
        pemWriter.writeObject(new PemObject("PRIVATE KEY", privateKey.getEncoded()));
        pemWriter.flush();

        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    public void start() throws IOException {
        server = serverBuilder.build();
        server.start();
        logger.debug("Started gRPC server on port " + this.server.getPort());
    }

    public void stop(final CountDownLatch shutdownLatch, int duration, TimeUnit unit) {
        Runnable shutdownThread =
                () -> {
                    try {
                        this.server.shutdown();
                        this.server.awaitTermination(duration, unit);
                    } catch (InterruptedException e) {
                        logger.warn(
                                "gRPC server was not able to terminate within timeout and was not shutdown gracefully");
                    } finally {
                        shutdownLatch.countDown();
                    }
                };

        shutdownThread.run();
    }

    private Executor createExecutor() {
        return (ThreadPoolExecutor) Executors.newFixedThreadPool(8);
    }
}
