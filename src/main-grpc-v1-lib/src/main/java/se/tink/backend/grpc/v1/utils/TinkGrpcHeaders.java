package se.tink.backend.grpc.v1.utils;

import io.grpc.Metadata;

public class TinkGrpcHeaders {
    public static final Metadata.Key<String> AUTHORIZATION = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);
    public static final Metadata.Key<String> ACCEPT_LANGUAGE = Metadata.Key.of("Accept-Language", Metadata.ASCII_STRING_MARSHALLER);
    public static final Metadata.Key<String> CLIENT_KEY_HEADER_NAME = Metadata.Key.of("X-Tink-Client-Key", Metadata.ASCII_STRING_MARSHALLER);
    public static final Metadata.Key<String> DEVICE_ID_HEADER_NAME = Metadata.Key.of("X-Tink-Device-ID", Metadata.ASCII_STRING_MARSHALLER);
    public static final Metadata.Key<String> OAUTH_CLIENT_ID_HEADER_NAME = Metadata.Key.of("X-Tink-OAuth-Client-ID", Metadata.ASCII_STRING_MARSHALLER);
    public static final Metadata.Key<String> USER_AGENT = Metadata.Key.of("User-Agent", Metadata.ASCII_STRING_MARSHALLER);
    @Deprecated
    public static final Metadata.Key<String> DEPRECATED_CLIENT_KEY = Metadata.Key
            .of("X-Tink-Deprecated-Client-Key", Metadata.ASCII_STRING_MARSHALLER);
    @Deprecated
    public static final Metadata.Key<String> DEVICE_UNAUTHORIZED = Metadata.Key
            .of("X-Tink-Device-Unauthorized", Metadata.ASCII_STRING_MARSHALLER);
    public static final Metadata.Key<String> REQUEST_ID = Metadata.Key.of("Request-Id", Metadata.ASCII_STRING_MARSHALLER);
}
