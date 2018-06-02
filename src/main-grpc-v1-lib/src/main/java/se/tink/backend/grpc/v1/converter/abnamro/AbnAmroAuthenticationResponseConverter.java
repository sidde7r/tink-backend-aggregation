package se.tink.backend.grpc.v1.converter.abnamro;

import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.backend.rpc.auth.AuthenticationResponse;
import se.tink.grpc.v1.models.AuthenticationStatus;
import se.tink.grpc.v1.rpc.AbnAmroAuthenticationResponse;

public class AbnAmroAuthenticationResponseConverter
        implements Converter<AuthenticationResponse, se.tink.grpc.v1.rpc.AbnAmroAuthenticationResponse> {

    @Override
    public se.tink.grpc.v1.rpc.AbnAmroAuthenticationResponse convertFrom(AuthenticationResponse input) {
        AbnAmroAuthenticationResponse.Builder builder = AbnAmroAuthenticationResponse.newBuilder();
        ConverterUtils.setIfPresent(input::getAuthenticationToken, builder::setAuthenticationToken);
        ConverterUtils.setIfPresent(input::getStatus, builder::setStatus, s ->
                EnumMappers.CORE_AUTHENTICATION_STATUS_TO_GRPC_MAP
                        .getOrDefault(s, AuthenticationStatus.AUTHENTICATION_STATUS_UNKNOWN));
        return builder.build();
    }
}

