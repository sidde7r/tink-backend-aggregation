package se.tink.backend.grpc.v1.converter.authentication;

import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.backend.rpc.auth.AuthenticationResponse;
import se.tink.grpc.v1.models.AuthenticationStatus;
import se.tink.grpc.v1.rpc.EmailAndPasswordAuthenticationResponse;

public class CoreAuthenticationResponseToGrpcConverter
        implements Converter<AuthenticationResponse, EmailAndPasswordAuthenticationResponse> {
    @Override
    public EmailAndPasswordAuthenticationResponse convertFrom(AuthenticationResponse input) {
        EmailAndPasswordAuthenticationResponse.Builder builder = EmailAndPasswordAuthenticationResponse.newBuilder();
        ConverterUtils.setIfPresent(input::getAuthenticationToken, builder::setAuthenticationToken);
        ConverterUtils.setIfPresent(input::getStatus, builder::setStatus, status ->
                EnumMappers.CORE_AUTHENTICATION_STATUS_TO_GRPC_MAP
                        .getOrDefault(status, AuthenticationStatus.AUTHENTICATION_STATUS_UNKNOWN));
        return builder.build();
    }
}
