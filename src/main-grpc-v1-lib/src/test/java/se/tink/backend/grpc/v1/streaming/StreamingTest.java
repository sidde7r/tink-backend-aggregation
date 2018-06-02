package se.tink.backend.grpc.v1.streaming;

import com.google.common.util.concurrent.Uninterruptibles;
import io.grpc.Channel;
import io.grpc.ClientInterceptors;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.grpc.v1.performance.HeaderMetadataTestInterceptor;
import se.tink.grpc.v1.rpc.EmailAndPasswordAuthenticationRequest;
import se.tink.grpc.v1.rpc.EmailAndPasswordAuthenticationResponse;
import se.tink.grpc.v1.rpc.LoginRequest;
import se.tink.grpc.v1.rpc.LoginResponse;
import se.tink.grpc.v1.rpc.StreamingRequest;
import se.tink.grpc.v1.rpc.StreamingResponse;
import se.tink.grpc.v1.services.AuthenticationServiceGrpc;
import se.tink.grpc.v1.services.EmailAndPasswordAuthenticationServiceGrpc;
import se.tink.grpc.v1.services.StreamingServiceGrpc;

@Ignore
public class StreamingTest {

    @Test
    public void streamTest() throws InterruptedException {

        gorun("erik@tink.se", "tink");

        System.out.println("Test done!1");

        Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);
    }

    private void gorun(String email, String password) throws InterruptedException {
        final DataWrapper dataWrapper = new DataWrapper();
        final StreamingWrapper streamingWrapper = new StreamingWrapper();

        Channel channel = NettyChannelBuilder.forAddress("localhost", 9998)
                .usePlaintext(true).keepAliveTime(30, TimeUnit.SECONDS).build();

        dataWrapper.deviceId = "914509f4-4f4b-43da-8211-8418d5f8a775";
        dataWrapper.clientKey = "f603e322cc0d00dd4b2f63088a6f776a";
        dataWrapper.userAgent = "test";

        EmailAndPasswordAuthenticationServiceGrpc.EmailAndPasswordAuthenticationServiceBlockingStub credentialsStub =
                EmailAndPasswordAuthenticationServiceGrpc.newBlockingStub(channel);

        EmailAndPasswordAuthenticationRequest emailAndPasswordAuthenticationRequest = EmailAndPasswordAuthenticationRequest
                .newBuilder()
                .setEmail(email)
                .setMarketCode("SE")
                .setPassword(password)
                .build();

        EmailAndPasswordAuthenticationResponse response = credentialsStub
                .emailAndPasswordAuthentication(emailAndPasswordAuthenticationRequest);

        dataWrapper.authenticationToken = response.getAuthenticationToken();

        AuthenticationServiceGrpc.AuthenticationServiceBlockingStub authenticationService = AuthenticationServiceGrpc
                .newBlockingStub(channel);

        LoginRequest loginRequest = LoginRequest.newBuilder()
                .setAuthenticationToken(response.getAuthenticationToken())
                .build();

        LoginResponse loginResponse = authenticationService.login(loginRequest);
        dataWrapper.sessionId = loginResponse.getSessionId();

        HeaderMetadataTestInterceptor interceptor = new HeaderMetadataTestInterceptor(dataWrapper.clientKey,
                dataWrapper.deviceId, dataWrapper.userAgent);

        interceptor.setSession(dataWrapper.sessionId);

        channel = ClientInterceptors.intercept(channel, interceptor);

        streamingWrapper.streamingConnection = streaming(channel, streamingWrapper);
    }

    public StreamingServiceGrpc.StreamingServiceStub streaming(Channel channel, StreamingWrapper wrapper) {
        StreamingServiceGrpc.StreamingServiceStub streamingServiceStub = StreamingServiceGrpc.newStub(channel);
        StreamObserver<StreamingRequest> request = streamingServiceStub.stream(new StreamObserver<StreamingResponse>() {
            @Override
            public void onNext(StreamingResponse streamingResponse) {
                wrapper.lastMessageReceived = new Date();

                System.out.println("Message received:");
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("We got an error!" + throwable.toString());
            }

            @Override
            public void onCompleted() {
                System.out.println("Things are completed!");
            }
        });

        request.onNext(StreamingRequest.newBuilder().build());

        return streamingServiceStub;
    }

    public class StreamingWrapper {
        public StreamingServiceGrpc.StreamingServiceStub streamingConnection;
        public Date lastMessageReceived;
    }

    public class DataWrapper {
        public String authenticationToken;
        public String sessionId;
        public String deviceId;
        public String clientKey;
        public String userAgent;
    }

}
