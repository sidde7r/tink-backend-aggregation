package se.tink.backend.grpc.v1.performance;

import com.google.common.base.Strings;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.stub.MetadataUtils;

public class HeaderMetadataTestInterceptor implements ClientInterceptor {
    public static final Metadata.Key<String> AUTHORIZATION = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);
    public static final Metadata.Key<String> DEVICE_ID_HEADER_NAME = Metadata.Key.of("X-Tink-Device-ID", Metadata.ASCII_STRING_MARSHALLER);
    public static final Metadata.Key<String> USER_AGENT = Metadata.Key.of("User-Agent", Metadata.ASCII_STRING_MARSHALLER);

    private final String clientKey;
    private final String deviceId;
    private String session;
    private String userAgent;

    public HeaderMetadataTestInterceptor(String clientKey, String deviceId, String userAgent) {
        this.clientKey = clientKey;
        this.deviceId = deviceId;
        this.userAgent = userAgent;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
                                                               CallOptions callOptions, final Channel next) {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                /* Set custom headers */
                if (!Strings.isNullOrEmpty(deviceId)) {
                    headers.put(DEVICE_ID_HEADER_NAME, deviceId);
                }
                if(!Strings.isNullOrEmpty(session)) {
                    headers.put(AUTHORIZATION, "Session " + session);
                }

                super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                    @Override
                    public void onHeaders(Metadata headers) {
                        MetadataUtils.newAttachHeadersInterceptor(headers);
                    }
                }, headers);
            }
        };

    }

    public void setSession(String session) {
        this.session = session;
    }

}

