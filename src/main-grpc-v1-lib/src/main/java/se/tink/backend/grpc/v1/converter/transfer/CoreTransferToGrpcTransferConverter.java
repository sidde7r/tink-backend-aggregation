package se.tink.backend.grpc.v1.converter.transfer;

import se.tink.libraries.uuid.UUIDUtils;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.backend.grpc.v1.utils.NumberUtils;
import se.tink.backend.grpc.v1.utils.ProtobufModelUtils;
import se.tink.grpc.v1.models.Transfer;

public class CoreTransferToGrpcTransferConverter
        implements Converter<se.tink.backend.core.transfer.Transfer, Transfer> {

    @Override
    public Transfer convertFrom(se.tink.backend.core.transfer.Transfer input) {
        Transfer.Builder builder = Transfer.newBuilder();
        ConverterUtils.setIfPresent(input::getAmount, builder::setAmount,
                amount -> NumberUtils.toCurrencyDenominatedAmount(amount.getValue(), amount.getCurrency()));
        ConverterUtils.setIfPresent(input::getCredentialsId, builder::setCredentialId, UUIDUtils::toTinkUUID);
        ConverterUtils.setIfPresent(input::getDestination, builder::setDestinationUri, AccountIdentifier::toUriAsString);
        ConverterUtils.setIfPresent(input::getDestinationMessage, builder::setDestinationMessage);
        ConverterUtils.setIfPresent(input::getId, builder::setId, UUIDUtils::toTinkUUID);
        ConverterUtils.setIfPresent(input::getSource, builder::setSourceUri, AccountIdentifier::toUriAsString);
        ConverterUtils.setIfPresent(input::getSourceMessage, builder::setSourceMessage);
        ConverterUtils.setIfPresent(input::getType, builder::setType,
                type -> EnumMappers.CORE_TRANSFER_TYPE_TO_GRPC_MAP.getOrDefault(type, Transfer.Type.TYPE_UNKNOWN));
        ConverterUtils.setIfPresent(input::getDueDate, builder::setDueDate, ProtobufModelUtils::toProtobufTimestamp);
        return builder.build();
    }
}
