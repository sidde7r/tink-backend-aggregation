package se.tink.backend.grpc.v1.converter.authentication;

import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.backend.rpc.auth.AuthenticationResponse;
import se.tink.grpc.v1.models.AuthenticationStatus;
import se.tink.grpc.v1.rpc.SignedChallengeAuthenticationResponse;

public class SignedChallengeAuthenticationResponseConverter implements
        Converter<AuthenticationResponse, SignedChallengeAuthenticationResponse> {

    @Override
    public SignedChallengeAuthenticationResponse convertFrom(AuthenticationResponse authenticationResponse) {
        SignedChallengeAuthenticationResponse.Builder builder = SignedChallengeAuthenticationResponse.newBuilder();
        ConverterUtils.setIfPresent(authenticationResponse::getAuthenticationToken, builder::setAuthenticationToken);
        ConverterUtils.setIfPresent(authenticationResponse::getStatus, builder::setStatus,
                status -> EnumMappers.CORE_AUTHENTICATION_STATUS_TO_GRPC_MAP.getOrDefault(status,
                        AuthenticationStatus.AUTHENTICATION_STATUS_UNKNOWN));
        return builder.build();
    }
}
