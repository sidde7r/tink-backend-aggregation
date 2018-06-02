package se.tink.backend.grpc.v1.converter.transaction;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import java.io.IOException;
import java.util.HashMap;
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

public class FirehoseTransactionToGrpcTransactionConverter
        implements Converter<se.tink.backend.firehose.v1.models.Transaction, Transaction> {
    private static final TypeReference<HashMap<TransactionPayloadTypes, String>> PAYLOAD_TYPE_REFERENCE = new TypeReference<HashMap<TransactionPayloadTypes, String>>() {
    };

    private final String currencyCode;
    private final Map<String, String> categoryCodeById;

    public FirehoseTransactionToGrpcTransactionConverter(String currencyCode,
            Map<String, String> categoryCodeById) {
        this.currencyCode = currencyCode;
        this.categoryCodeById = categoryCodeById;
    }

    @Override
    public Transaction convertFrom(se.tink.backend.firehose.v1.models.Transaction input) {
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
        ConverterUtils.setIfPresent(input::getPending, builder::setPending);
        ConverterUtils.setIfPresent(input::getUpcoming, builder::setUpcoming);
        ConverterUtils.setIfPresent(input::getType, builder::setType,
                type -> EnumMappers.FIREHOSE_TRANSACTION_TYPE_TO_GRPC_MAP
                        .getOrDefault(type, Transaction.Type.TYPE_UNKNOWN));

        if (!Strings.isNullOrEmpty(input.getPayloadSerialized())) {
            try {
                Map<TransactionPayloadTypes, String> map = new ObjectMapper().readValue(input.getPayloadSerialized(),
                        PAYLOAD_TYPE_REFERENCE);

                Transaction.TransactionDetails.Builder transactionDetailsBuilder = Transaction.TransactionDetails.newBuilder();
                String transferId = map.get(TransactionPayloadTypes.EDITABLE_TRANSACTION_TRANSFER_ID);
                if (!Strings.isNullOrEmpty(transferId)) {
                    transactionDetailsBuilder.setTransferId(transferId);
                }
                builder.setDetails(transactionDetailsBuilder);

                String secondaryDescription = map.get(TransactionPayloadTypes.MESSAGE);
                if (!Strings.isNullOrEmpty(secondaryDescription)) {
                    builder.setSecondaryDescription(secondaryDescription);
                }
            } catch (IOException e) {
                // NOP
            }
        }

        return builder.build();
    }

}
