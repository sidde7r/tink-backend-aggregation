package se.tink.backend.grpc.v1.converter.transfer;

import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.backend.grpc.v1.utils.ProtobufModelUtils;
import se.tink.grpc.v1.models.SignableOperation;

public class CoreSignableOperationToGrpcSignableOperationConverter
        implements Converter<se.tink.backend.core.signableoperation.SignableOperation, SignableOperation> {
    @Override
    public SignableOperation convertFrom(se.tink.backend.core.signableoperation.SignableOperation input) {
        SignableOperation.Builder builder = SignableOperation.newBuilder();
        ConverterUtils.setIfPresent(input::getCreated, builder::setCreated, ProtobufModelUtils::toProtobufTimestamp);
        ConverterUtils.setIfPresent(input::getId, builder::setId, UUIDUtils::toTinkUUID);
        ConverterUtils.setIfPresent(input::getStatus, builder::setStatus,
                status -> EnumMappers.CORE_SIGNABLE_OPERATION_STATUS_TO_GRPC_MAP
                        .getOrDefault(status, SignableOperation.Status.STATUS_UNKNOWN));
        ConverterUtils.setIfPresent(input::getStatusMessage, builder::setStatusMessage);
        ConverterUtils.setIfPresent(input::getType, builder::setType,
                type -> EnumMappers.CORE_SIGNABLE_OPERATION_TYPE_TO_GRPC_MAP
                        .getOrDefault(type, SignableOperation.Type.TYPE_UNKNOWN));
        ConverterUtils.setIfPresent(input::getUnderlyingId, builder::setUnderlyingId, UUIDUtils::toTinkUUID);
        ConverterUtils.setIfPresent(input::getUpdated, builder::setUpdated, ProtobufModelUtils::toProtobufTimestamp);
        ConverterUtils.setIfPresent(input::getCredentialsId, builder::setCredentialId, UUIDUtils::toTinkUUID);
        return builder.build();
    }
}
