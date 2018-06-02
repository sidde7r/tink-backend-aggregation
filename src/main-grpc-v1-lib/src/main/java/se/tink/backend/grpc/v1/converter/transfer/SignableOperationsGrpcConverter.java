package se.tink.backend.grpc.v1.converter.transfer;

import java.util.List;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.grpc.v1.models.SignableOperations;

public class SignableOperationsGrpcConverter {
    private CoreSignableOperationToGrpcSignableOperationConverter coreSignableOperationToGrpcSignableOperationConverter;

    public SignableOperationsGrpcConverter(
            CoreSignableOperationToGrpcSignableOperationConverter coreSignableOperationToGrpcSignableOperationConverter) {
        this.coreSignableOperationToGrpcSignableOperationConverter = coreSignableOperationToGrpcSignableOperationConverter;
    }

    public SignableOperations convertFrom(List<SignableOperation> input) {
        List<se.tink.grpc.v1.models.SignableOperation> signableOperations = coreSignableOperationToGrpcSignableOperationConverter.convertFrom(input);
        return SignableOperations.newBuilder().addAllSignableOperation(signableOperations).build();
    }
}
