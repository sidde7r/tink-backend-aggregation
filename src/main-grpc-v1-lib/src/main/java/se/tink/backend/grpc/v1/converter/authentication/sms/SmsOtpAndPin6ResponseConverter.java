package se.tink.backend.grpc.v1.converter.authentication.sms;

import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.backend.rpc.auth.AuthenticationResponse;
import se.tink.grpc.v1.models.AuthenticationStatus;
import se.tink.grpc.v1.rpc.SmsOtpAndPin6AuthenticationResponse;

public class SmsOtpAndPin6ResponseConverter
        implements Converter<AuthenticationResponse, SmsOtpAndPin6AuthenticationResponse> {

    @Override
    public SmsOtpAndPin6AuthenticationResponse convertFrom(AuthenticationResponse input) {
        SmsOtpAndPin6AuthenticationResponse.Builder builder = SmsOtpAndPin6AuthenticationResponse.newBuilder();
        ConverterUtils.setIfPresent(input::getAuthenticationToken, builder::setAuthenticationToken);
        ConverterUtils.setIfPresent(input::getStatus, builder::setStatus, s ->
                EnumMappers.CORE_AUTHENTICATION_STATUS_TO_GRPC_MAP
                        .getOrDefault(s, AuthenticationStatus.AUTHENTICATION_STATUS_UNKNOWN));
        return builder.build();
    }
}
