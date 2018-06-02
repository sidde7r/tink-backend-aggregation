package se.tink.backend.grpc.v1.converter.transaction;

import com.google.protobuf.StringValue;
import java.util.stream.Collectors;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.utils.ProtobufModelUtils;
import se.tink.backend.rpc.UpdateTransactionRequest;
import se.tink.grpc.v1.models.Tag;

public class UpdateTransactionRequestConverter
        implements Converter<se.tink.grpc.v1.rpc.UpdateTransactionRequest, UpdateTransactionRequest> {
    @Override
    public UpdateTransactionRequest convertFrom(se.tink.grpc.v1.rpc.UpdateTransactionRequest input) {
        UpdateTransactionRequest updateRequest = new UpdateTransactionRequest();
        ConverterUtils.setIfPresent(input::hasDescription, input::getDescription, updateRequest::setDescription,
                StringValue::getValue);
        ConverterUtils.setIfPresent(input::hasDate, input::getDate, updateRequest::setDate,
                ProtobufModelUtils::timestampToDate);
        ConverterUtils.setIfPresent(input::hasNotes, input::getNotes, updateRequest::setNotes, StringValue::getValue);
        ConverterUtils.setIfPresent(input::getTagsList, updateRequest::setTags,
                tags -> tags.stream().map(Tag::getName)
                        .collect(Collectors.toList()));
        return updateRequest;
    }
}
