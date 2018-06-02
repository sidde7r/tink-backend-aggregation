package se.tink.backend.grpc.v1.converter.authentication.sms;

import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.backend.rpc.auth.otp.VerifySmsOtpResponse;
import se.tink.grpc.v1.models.SmsOtpStatus;

public class VerifySmsOtpResponseConverter
        implements Converter<VerifySmsOtpResponse, se.tink.grpc.v1.rpc.VerifySmsOtpResponse> {

    @Override
    public se.tink.grpc.v1.rpc.VerifySmsOtpResponse convertFrom(VerifySmsOtpResponse input) {
        se.tink.grpc.v1.rpc.VerifySmsOtpResponse.Builder builder = se.tink.grpc.v1.rpc.VerifySmsOtpResponse
                .newBuilder();

        ConverterUtils.setIfPresent(input::getToken, builder::setSmsOtpVerificationToken);
        ConverterUtils.setIfPresent(input::isExistingUser, builder::setExistingUser);
        ConverterUtils.setIfPresent(input::getResult, builder::setStatus,
                t -> EnumMappers.CORE_SMS_OTP_STATUS_TO_GRPC_MAP.getOrDefault(t, SmsOtpStatus.SMS_OTP_STATUS_UNKNOWN));

        return builder.build();
    }
}
