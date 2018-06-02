package se.tink.backend.grpc.v1.converter.application;

import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.backend.grpc.v1.utils.ProtobufModelUtils;
import se.tink.grpc.v1.models.ApplicationStatus;
import se.tink.grpc.v1.models.ApplicationStatusKey;

public class ApplicationStatusConverter {
    public static ApplicationStatus convert(se.tink.backend.core.ApplicationStatus input) {
        ApplicationStatus.Builder builder = ApplicationStatus.newBuilder();
        builder.setKey(EnumMappers.APPLICATION_STATUS_KEY_TO_GRPC.getOrDefault(input.getKey(), ApplicationStatusKey.APPLICATION_STATUS_UNKNOWN));
        ConverterUtils.setIfPresent(input::getMessage, builder::setMessage);
        ConverterUtils.setIfPresent(input::getUpdated, builder::setUpdated, ProtobufModelUtils::toProtobufTimestamp);
        return builder.build();
    }
}
