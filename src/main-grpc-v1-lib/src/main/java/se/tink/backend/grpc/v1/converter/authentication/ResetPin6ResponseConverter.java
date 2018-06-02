package se.tink.backend.grpc.v1.converter.authentication;

import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.backend.rpc.auth.AuthenticationResponse;
import se.tink.grpc.v1.models.AuthenticationStatus;
import se.tink.grpc.v1.rpc.ResetPin6Response;

public class ResetPin6ResponseConverter implements Converter<AuthenticationResponse, ResetPin6Response> {

    @Override
    public ResetPin6Response convertFrom(AuthenticationResponse input) {
        ResetPin6Response.Builder builder = ResetPin6Response.newBuilder();
        ConverterUtils.setIfPresent(input::getAuthenticationToken, builder::setAuthenticationToken);
        ConverterUtils.setIfPresent(input::getStatus, builder::setStatus, status ->
                EnumMappers.CORE_AUTHENTICATION_STATUS_TO_GRPC_MAP
                        .getOrDefault(status, AuthenticationStatus.AUTHENTICATION_STATUS_UNKNOWN));
        return builder.build();
    }
}
