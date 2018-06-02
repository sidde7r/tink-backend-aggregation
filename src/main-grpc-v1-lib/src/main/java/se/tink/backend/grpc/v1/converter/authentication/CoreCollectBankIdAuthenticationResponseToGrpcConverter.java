package se.tink.backend.grpc.v1.converter.authentication;

import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.grpc.v1.models.BankIdAuthenticationStatus;
import se.tink.grpc.v1.rpc.CollectBankIdAuthenticationResponse;

public class CoreCollectBankIdAuthenticationResponseToGrpcConverter implements
        Converter<se.tink.backend.rpc.auth.bankid.CollectBankIdAuthenticationResponse, CollectBankIdAuthenticationResponse> {
    @Override
    public CollectBankIdAuthenticationResponse convertFrom(
            se.tink.backend.rpc.auth.bankid.CollectBankIdAuthenticationResponse input) {
        CollectBankIdAuthenticationResponse.Builder builder = CollectBankIdAuthenticationResponse.newBuilder();
        ConverterUtils.setIfPresent(input::getNationalId, builder::setNationalId);
        ConverterUtils.setIfPresent(input::getStatus, builder::setStatus, status ->
                EnumMappers.CORE_BANK_ID_STATUS_TO_GRPC_MAP.getOrDefault(status,
                        BankIdAuthenticationStatus.BANK_ID_AUTHENTICATION_STATUS_UNKNOWN));

        return builder.build();
    }
}
