package se.tink.backend.grpc.v1.converter.authentication.sms;

import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.utils.ProtobufModelUtils;
import se.tink.backend.rpc.auth.otp.InitiateSmsOtpResponse;

public class InitiateSmsOtpResponseConverter
        implements Converter<InitiateSmsOtpResponse, se.tink.grpc.v1.rpc.InitiateSmsOtpResponse> {

    @Override
    public se.tink.grpc.v1.rpc.InitiateSmsOtpResponse convertFrom(InitiateSmsOtpResponse input) {
        se.tink.grpc.v1.rpc.InitiateSmsOtpResponse.Builder builder = se.tink.grpc.v1.rpc.InitiateSmsOtpResponse
                .newBuilder();

        ConverterUtils.setIfPresent(input::getToken, builder::setSmsOtpVerificationToken);
        ConverterUtils.setIfPresent(input::getExpireAt, builder::setExpireAt, ProtobufModelUtils::toProtobufTimestamp);
        ConverterUtils.setIfPresent(input::getOtpLength, builder::setOtpLength);

        return builder.build();
    }
}

