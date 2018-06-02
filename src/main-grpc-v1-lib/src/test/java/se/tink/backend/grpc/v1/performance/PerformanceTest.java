package se.tink.backend.grpc.v1.performance;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.grpc.Channel;
import io.grpc.ClientInterceptors;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.grpc.v1.models.Credential;
import se.tink.grpc.v1.rpc.CreateCredentialRequest;
import se.tink.grpc.v1.rpc.CreateCredentialResponse;
import se.tink.grpc.v1.rpc.EmailAndPasswordAuthenticationRequest;
import se.tink.grpc.v1.rpc.EmailAndPasswordAuthenticationResponse;
import se.tink.grpc.v1.rpc.RegisterRequest;
import se.tink.grpc.v1.rpc.RegisterResponse;
import se.tink.grpc.v1.rpc.StreamingRequest;
import se.tink.grpc.v1.rpc.StreamingResponse;
import se.tink.grpc.v1.services.AuthenticationServiceGrpc;
import se.tink.grpc.v1.services.CredentialServiceGrpc;
import se.tink.grpc.v1.services.EmailAndPasswordAuthenticationServiceGrpc;
import se.tink.grpc.v1.services.StreamingServiceGrpc;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;



// In order to setup a client you would need to go into etc/development-main-server.yml and add `EMAIL_AND_PASSWORD` as `marketRegisterMethods`
@Ignore
public class PerformanceTest {

    private List<StreamingWrapper> streamingConnections;
    private int messages;
    private String serverAddress;
    private int port;

    @Test
    public void streamTest() throws InterruptedException {
        streamingConnections = Lists.newArrayList();
        messages = 0;
        IntStream.range(0, 100).parallel().forEach(i->{
            try {
                gorun(i);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        });

        System.out.println("checking open connections");

        while(streamingConnections.size()>0) {
            System.out.println("size: "+streamingConnections.size());
            for (int j=0; j<streamingConnections.size(); j++) {
                Calendar calendar = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
                calendar.add(Calendar.SECOND, -5);
                Date beforeXSeconds = calendar.getTime();

                if (streamingConnections.get(j).lastMessageReceived.compareTo(beforeXSeconds) <= 0) {
                    streamingConnections.remove(j);
                    continue;
                }
            }
        }
        System.out.println("Messages received: " + messages);

    }

    private void gorun(int i) throws InterruptedException {
        final DataWrapper dataWrapper = new DataWrapper();
        final StreamingWrapper streamingWrapper = new StreamingWrapper();
        serverAddress = "localhost";
        port = 9998;
        System.out.println(i);
        Channel channel = NettyChannelBuilder.forAddress(serverAddress, port)
                .usePlaintext(true).keepAliveTime(30, TimeUnit.SECONDS).build();

        final String email = "random_"+ i+ "@email.com";
        String market = "SE";
        String password = "1234";
        dataWrapper.deviceId = UUID.randomUUID().toString();

        EmailAndPasswordAuthenticationServiceGrpc.EmailAndPasswordAuthenticationServiceBlockingStub credentialsStub = EmailAndPasswordAuthenticationServiceGrpc.newBlockingStub(channel);


        EmailAndPasswordAuthenticationRequest emailAndPasswordAuthenticationRequest = EmailAndPasswordAuthenticationRequest.newBuilder()
                .setEmail(email)
                .setMarketCode(market)
                .setPassword(password)
                .build();

        EmailAndPasswordAuthenticationResponse emailAndPasswordAuthenticationResponse = credentialsStub.emailAndPasswordAuthentication(emailAndPasswordAuthenticationRequest);
        dataWrapper.authenticationToken = emailAndPasswordAuthenticationResponse.getAuthenticationToken();
        dataWrapper.email = email;
        dataWrapper.clientKey = email;
        dataWrapper.userAgent = "test";

        AuthenticationServiceGrpc.AuthenticationServiceBlockingStub authenticationService = AuthenticationServiceGrpc.newBlockingStub(channel);

        RegisterRequest registerRequest = RegisterRequest.newBuilder().setAuthenticationToken(dataWrapper.authenticationToken).setEmail(dataWrapper.email).setLocale("sv_SE").build();

        RegisterResponse registerResponse = authenticationService.register(registerRequest);
        dataWrapper.sessionId = registerResponse.getSessionId();


        HeaderMetadataTestInterceptor interceptor = new HeaderMetadataTestInterceptor(dataWrapper.clientKey, dataWrapper.deviceId, dataWrapper.userAgent);
        interceptor.setSession(dataWrapper.sessionId);
        channel = ClientInterceptors.intercept(channel, interceptor);


        streamingWrapper.streamingConnection = streaming(channel, email, streamingWrapper);
        streamingConnections.add(streamingWrapper);

        Thread.sleep(100L);
        Map fields = Maps.newHashMap();
        fields.put("username", "201212121212");
        fields.put("password", "1212");
        CredentialServiceGrpc.CredentialServiceBlockingStub credentialServiceBlockingStub = CredentialServiceGrpc.newBlockingStub(channel);
        CreateCredentialRequest createCredentialRequest = CreateCredentialRequest.newBuilder().setProviderName("avanza").setType(Credential.Type.TYPE_MOBILE_BANKID).putAllFields(fields).build();
        CreateCredentialResponse r = credentialServiceBlockingStub.createCredential(createCredentialRequest);


    }

    public StreamingServiceGrpc.StreamingServiceStub streaming(Channel channel, String email, StreamingWrapper wrapper) {
        StreamingServiceGrpc.StreamingServiceStub streamingServiceStub = StreamingServiceGrpc.newStub(channel);
        StreamObserver<StreamingRequest> request = streamingServiceStub.stream(new StreamObserver<StreamingResponse>() {
            @Override
            public void onNext(StreamingResponse streamingResponse) {
                wrapper.lastMessageReceived = new Date();
                messages++;
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {
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
        public String email;
    }

}
