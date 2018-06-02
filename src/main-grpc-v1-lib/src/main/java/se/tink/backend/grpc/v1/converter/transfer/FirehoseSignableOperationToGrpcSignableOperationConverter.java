package se.tink.backend.grpc.v1.converter.transfer;

import java.util.List;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.backend.grpc.v1.utils.ProtobufModelUtils;
import se.tink.grpc.v1.models.SignableOperation;
import se.tink.grpc.v1.models.SignableOperations;

public class FirehoseSignableOperationToGrpcSignableOperationConverter {

    public SignableOperations convertFrom(List<se.tink.backend.firehose.v1.models.SignableOperation> input) {
        SignableOperations.Builder signableOperationsBuilder = SignableOperations.newBuilder();

        for (se.tink.backend.firehose.v1.models.SignableOperation signableOperation : input) {
            SignableOperation.Builder builder = SignableOperation.newBuilder();
            ConverterUtils.setIfPresent(signableOperation::getCreated, builder::setCreated, ProtobufModelUtils::toProtobufTimestamp);
            ConverterUtils.setIfPresent(signableOperation::getId, builder::setId);
            ConverterUtils.setIfPresent(signableOperation::getStatus, builder::setStatus,
                    status -> EnumMappers.FIREHOSE_SIGNABLE_OPERATION_STATUS_TO_GRPC_MAP
                            .getOrDefault(status, SignableOperation.Status.STATUS_UNKNOWN));
            ConverterUtils.setIfPresent(signableOperation::getStatusMessage, builder::setStatusMessage);
            ConverterUtils.setIfPresent(signableOperation::getType, builder::setType,
                    type -> EnumMappers.FIREHOSE_SIGNABLE_OPERATION_TYPE_TO_GRPC_MAP
                            .getOrDefault(type, SignableOperation.Type.TYPE_UNKNOWN));
            ConverterUtils.setIfPresent(signableOperation::getUnderlyingId, builder::setUnderlyingId);
            ConverterUtils.setIfPresent(signableOperation::getUpdated, builder::setUpdated, ProtobufModelUtils::toProtobufTimestamp);
            ConverterUtils.setIfPresent(signableOperation::getCredentialsId, builder::setCredentialId);
            signableOperationsBuilder.addSignableOperation(builder);
        }
        return signableOperationsBuilder.build();
    }
}
