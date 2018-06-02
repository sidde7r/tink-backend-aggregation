package se.tink.backend.grpc.v1.converter.authentication;

import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.grpc.v1.models.BankIdAuthenticationStatus;
import se.tink.grpc.v1.rpc.InitiateBankIdAuthenticationResponse;

public class CoreInitiateBankIdAuthenticationResponseToGrpcConverter  implements
        Converter<se.tink.backend.rpc.auth.bankid.InitiateBankIdAuthenticationResponse, InitiateBankIdAuthenticationResponse> {

    @Override
    public InitiateBankIdAuthenticationResponse convertFrom(
            se.tink.backend.rpc.auth.bankid.InitiateBankIdAuthenticationResponse input) {
        InitiateBankIdAuthenticationResponse.Builder builder = InitiateBankIdAuthenticationResponse.newBuilder();
        ConverterUtils.setIfPresent(input::getAuthenticationToken, builder::setAuthenticationToken);
        ConverterUtils.setIfPresent(input::getAutostartToken, builder::setAutoStartToken);
        ConverterUtils.setIfPresent(input::getStatus, status ->
                EnumMappers.CORE_BANK_ID_STATUS_TO_GRPC_MAP.getOrDefault(status,
                        BankIdAuthenticationStatus.BANK_ID_AUTHENTICATION_STATUS_UNKNOWN));
        return builder.build();

    }
}
