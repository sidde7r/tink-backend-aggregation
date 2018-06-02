package se.tink.backend.grpc.v1.converter.transaction;

import com.google.common.base.Strings;
import java.util.Map;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.backend.grpc.v1.utils.NumberUtils;
import se.tink.backend.grpc.v1.utils.ProtobufModelUtils;
import se.tink.backend.utils.TagsUtils;
import se.tink.grpc.v1.models.Tag;
import se.tink.grpc.v1.models.Transaction;

public class CoreTransactionToGrpcTransactionConverter
        implements Converter<se.tink.backend.core.Transaction, Transaction> {
    private final String currencyCode;
    private final Map<String, String> categoryCodeById;

    public CoreTransactionToGrpcTransactionConverter(String currencyCode,
            Map<String, String> categoryCodeById) {
        this.currencyCode = currencyCode;
        this.categoryCodeById = categoryCodeById;
    }

    @Override
    public Transaction convertFrom(se.tink.backend.core.Transaction input) {
        Transaction.Builder builder = Transaction.newBuilder();
        ConverterUtils.setIfPresent(input::getAccountId, builder::setAccountId);
        builder.setAmount(NumberUtils.toCurrencyDenominatedAmount(input.getAmount(), currencyCode));
        ConverterUtils.setIfPresent(input::getCategoryId, builder::setCategoryCode, categoryCodeById::get);
        builder.setOriginalAmount(NumberUtils.toCurrencyDenominatedAmount(input.getOriginalAmount(), currencyCode));
        ConverterUtils.setIfPresent(input::getDate, builder::setDate, ProtobufModelUtils::toProtobufTimestamp);
        ConverterUtils.setIfPresent(input::getDescription, builder::setDescription);
        ConverterUtils.setIfPresent(input::getId, builder::setId);
        ConverterUtils.setIfPresent(input::getNotes, builder::setNotes, TagsUtils::removeTrailingTags);
        TagsUtils.extractUniqueTags(input.getNotes()).stream()
                .map(tag -> Tag.newBuilder().setName(tag).build())
                .forEach(builder::addTags);
        ConverterUtils.setIfPresent(input::getOriginalDate, builder::setOriginalDate,
                ProtobufModelUtils::toProtobufTimestamp);
        ConverterUtils.setIfPresent(input::getOriginalDescription, builder::setOriginalDescription);
        ConverterUtils.setIfPresent(input::isPending, builder::setPending);
        ConverterUtils.setIfPresent(input::isUpcoming, builder::setUpcoming);
        ConverterUtils.setIfPresent(input::getType, builder::setType,
                type -> EnumMappers.CORE_TRANSACTION_TYPE_TO_GRPC_MAP
                        .getOrDefault(type, Transaction.Type.TYPE_UNKNOWN));
        ConverterUtils.setIfPresent(input::getInserted, builder::setInserted, ProtobufModelUtils::toProtobufTimestamp);

        Transaction.TransactionDetails.Builder transactionDetailsBuilder = Transaction.TransactionDetails.newBuilder();
        String transferId = input.getPayload().get(TransactionPayloadTypes.EDITABLE_TRANSACTION_TRANSFER_ID);
        if (!Strings.isNullOrEmpty(transferId)) {
            transactionDetailsBuilder.setTransferId(transferId);
        }

        String secondaryDescription = input.getPayload().get(TransactionPayloadTypes.MESSAGE);
        if (!Strings.isNullOrEmpty(secondaryDescription)) {
            builder.setSecondaryDescription(secondaryDescription);
        }

        builder.setDetails(transactionDetailsBuilder);

        return builder.build();
    }
}
