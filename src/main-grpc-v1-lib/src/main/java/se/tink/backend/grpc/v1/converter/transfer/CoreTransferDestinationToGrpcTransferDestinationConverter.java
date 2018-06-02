package se.tink.backend.grpc.v1.converter.transfer;

import java.net.URI;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.backend.grpc.v1.converter.provider.CoreImageUrlsToGrpcImagesConverter;
import se.tink.backend.grpc.v1.utils.NumberUtils;
import se.tink.grpc.v1.models.Account;
import se.tink.grpc.v1.models.TransferDestination;

public class CoreTransferDestinationToGrpcTransferDestinationConverter
        implements Converter<se.tink.backend.core.transfer.TransferDestination, TransferDestination> {
    private final CoreImageUrlsToGrpcImagesConverter imagesConverter = new CoreImageUrlsToGrpcImagesConverter();
    private final String currencyCode;

    public CoreTransferDestinationToGrpcTransferDestinationConverter(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    @Override
    public TransferDestination convertFrom(se.tink.backend.core.transfer.TransferDestination input) {
        TransferDestination.Builder builder = TransferDestination.newBuilder();
        ConverterUtils.setIfPresent(input::getUri, builder::setUri, URI::toString);
        ConverterUtils.setIfPresent(input::getName, builder::setName);
        ConverterUtils.setIfPresent(input::getBalance, builder::setBalance,
                balance -> NumberUtils.toCurrencyDenominatedAmount(balance, currencyCode));
        ConverterUtils.setIfPresent(input::getDisplayBankName, builder::setDisplayBankName);
        ConverterUtils.setIfPresent(input::getType, builder::setType,
                type -> EnumMappers.CORE_ACCOUNT_TYPE_TO_GRPC_MAP
                        .getOrDefault(AccountTypes.valueOf(type), Account.Type.TYPE_UNKNOWN));
        ConverterUtils.setIfPresent(input::getImages, builder::setImages, imagesConverter::convertFrom);
        ConverterUtils.setIfPresent(input::getDisplayAccountNumber, builder::setDisplayAccountNumber);
        ConverterUtils.setIfPresent(input::isMatchesMultiple, builder::setMatchesMultiple);
        return builder.build();
    }
}
