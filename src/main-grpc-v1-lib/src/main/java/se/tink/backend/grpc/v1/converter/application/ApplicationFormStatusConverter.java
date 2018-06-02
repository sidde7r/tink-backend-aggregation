package se.tink.backend.grpc.v1.converter.application;


import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.backend.grpc.v1.utils.ProtobufModelUtils;
import se.tink.grpc.v1.models.ApplicationFormStatus;
import se.tink.grpc.v1.models.ApplicationFormStatusKey;

public class ApplicationFormStatusConverter {
    public static ApplicationFormStatus convert(se.tink.backend.core.ApplicationFormStatus input) {
        ApplicationFormStatus.Builder builder = ApplicationFormStatus.newBuilder();
        builder.setKey(EnumMappers.APPLICATION_FORM_STATUS_KEY_TO_GRPC.getOrDefault(input.getKey(), ApplicationFormStatusKey.APPLICATION_FORM_STATUS_UNKNOWN));
        ConverterUtils.setIfPresent(input::getMessage, builder::setMessage);
        ConverterUtils.setIfPresent(input::getUpdated, builder::setUpdated, ProtobufModelUtils::toProtobufTimestamp);
        return builder.build();
    }
}
