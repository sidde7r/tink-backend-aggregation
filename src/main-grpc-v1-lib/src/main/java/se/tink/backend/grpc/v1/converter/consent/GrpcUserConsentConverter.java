package se.tink.backend.grpc.v1.converter.consent;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.consent.core.UserConsent;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.backend.grpc.v1.utils.ProtobufModelUtils;
import se.tink.grpc.v1.models.ConsentAction;

public class GrpcUserConsentConverter {

    public static List<se.tink.grpc.v1.models.UserConsent> convert(List<UserConsent> input) {
        return input.stream().map(GrpcUserConsentConverter::convert).collect(Collectors.toList());
    }

    public static se.tink.grpc.v1.models.UserConsent convert(UserConsent input) {
        se.tink.grpc.v1.models.UserConsent.Builder builder = se.tink.grpc.v1.models.UserConsent.newBuilder();

        ConverterUtils.setIfPresent(input::getId, builder::setId);
        ConverterUtils.setIfPresent(input::getKey, builder::setKey);
        ConverterUtils.setIfPresent(input::getVersion, builder::setVersion);
        ConverterUtils.setIfPresent(input::getAction, builder::setAction,
                t -> EnumMappers.CORE_CONSENT_ACTION_TO_GRPC_MAP.getOrDefault(t, ConsentAction.CONSENT_ACTION_UNKNOWN));
        ConverterUtils
                .setIfPresent(input::getTimestamp, builder::setTimestamp, ProtobufModelUtils::toProtobufTimestamp);

        return builder.build();
    }
}
