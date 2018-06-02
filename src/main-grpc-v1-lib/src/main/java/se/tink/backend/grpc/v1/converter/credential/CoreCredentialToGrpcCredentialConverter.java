package se.tink.backend.grpc.v1.converter.credential;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Strings;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import se.tink.backend.common.payloads.MobileBankIdAuthenticationPayload;
import se.tink.backend.common.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Field;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.backend.grpc.v1.converter.provider.CoreFieldsToGrpcProviderFieldsConverter;
import se.tink.backend.grpc.v1.utils.ProtobufModelUtils;
import se.tink.grpc.v1.models.Credential;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CoreCredentialToGrpcCredentialConverter implements Converter<Credentials, Credential> {
    private static final TypeReference<List<Field>> LIST_OF_FIELDS_TYPE_REFERENCE = new TypeReference<List<Field>>() {
    };
    private CoreFieldsToGrpcProviderFieldsConverter fieldsConverter = new CoreFieldsToGrpcProviderFieldsConverter();
    private CoreThirdPartyAppAuthenticationPayloadToGrpcConverter thirdPartyAppConverter = new CoreThirdPartyAppAuthenticationPayloadToGrpcConverter();

    private Locale locale;

    public CoreCredentialToGrpcCredentialConverter(Locale locale) {
        this.locale = locale;
    }

    @Override
    public Credential convertFrom(Credentials input) {
        Credential.Builder builder = Credential.newBuilder();
        ConverterUtils.setIfPresent(input::getId, builder::setId);
        ConverterUtils.setIfPresent(input::getProviderName, builder::setProviderName);
        ConverterUtils.setIfPresent(input::getType, builder::setType,
                type -> EnumMappers.CORE_CREDENTIALS_TYPE_TO_GRPC_MAP.getOrDefault(type, Credential.Type.TYPE_UNKNOWN));
        ConverterUtils.setIfPresent(input::getStatus, builder::setStatus,
                status -> EnumMappers.CORE_CREDENTIALS_STATUS_TO_GRPC_MAP
                        .getOrDefault(status, Credential.Status.STATUS_UNKNOWN));
        ConverterUtils.setIfPresent(input::getStatusPayload, builder::setStatusPayload);
        ConverterUtils.setIfPresent(input::getStatusUpdated, builder::setStatusUpdated,
                ProtobufModelUtils::toProtobufTimestamp);
        ConverterUtils.setIfPresent(input::getUpdated, builder::setUpdated, ProtobufModelUtils::toProtobufTimestamp);
        ConverterUtils.setIfPresent(input::getFields, builder::putAllFields);

        final String supplementalInformation = input.getSupplementalInformation();

        switch (input.getStatus()) {
        case AWAITING_MOBILE_BANKID_AUTHENTICATION:
            ThirdPartyAppAuthenticationPayload mobileBankIdPayload = MobileBankIdAuthenticationPayload
                    .create(supplementalInformation, input.getId(), locale);
            builder.setThirdPartyAppAuthentication(thirdPartyAppConverter.convertFrom(mobileBankIdPayload));
            break;
        case AWAITING_THIRD_PARTY_APP_AUTHENTICATION:
            Optional<ThirdPartyAppAuthenticationPayload> payload = deserializeThirdPartyAppPayload(
                    supplementalInformation);

            payload.ifPresent(x -> builder.setThirdPartyAppAuthentication(thirdPartyAppConverter.convertFrom(x)));
            break;
        case AWAITING_SUPPLEMENTAL_INFORMATION:
            Optional<List<Field>> fields = deserializeFields(supplementalInformation);

            fields.ifPresent(x -> builder.addAllSupplementalInformationFields(fieldsConverter.convertFrom(x)));
            break;
        default:
            break;
        }

        return builder.build();
    }

    private static Optional<List<Field>> deserializeFields(String input) {
        if (Strings.isNullOrEmpty(input)) {
            return Optional.empty();
        }

        return Optional.ofNullable(SerializationUtils.deserializeFromString(input, LIST_OF_FIELDS_TYPE_REFERENCE));
    }

    private static Optional<ThirdPartyAppAuthenticationPayload> deserializeThirdPartyAppPayload(String input) {
        if (Strings.isNullOrEmpty(input)) {
            return Optional.empty();
        }

        return Optional.ofNullable(
                SerializationUtils.deserializeFromString(input, ThirdPartyAppAuthenticationPayload.class));
    }
}
