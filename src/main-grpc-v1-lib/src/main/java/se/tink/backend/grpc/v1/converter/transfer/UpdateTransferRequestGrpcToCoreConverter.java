package se.tink.backend.grpc.v1.converter.transfer;

import com.google.protobuf.StringValue;
import java.net.URI;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.utils.NumberUtils;
import se.tink.backend.grpc.v1.utils.ProtobufModelUtils;
import se.tink.backend.rpc.UpdateTransferRequest;
import se.tink.libraries.account.AccountIdentifier;

public class UpdateTransferRequestGrpcToCoreConverter
        implements Converter<se.tink.grpc.v1.rpc.UpdateTransferRequest,UpdateTransferRequest> {
    @Override
    public UpdateTransferRequest convertFrom(se.tink.grpc.v1.rpc.UpdateTransferRequest input) {
        UpdateTransferRequest updateRequest = new UpdateTransferRequest();
        ConverterUtils
                .setIfPresent(input::hasAmount, input::getAmount, updateRequest::setAmount, NumberUtils::toAmount);
        ConverterUtils.setIfPresent(input::hasDestination, input::getDestination, updateRequest::setDestination,
                destination -> AccountIdentifier
                .create(URI.create(destination.getValue())));
        ConverterUtils.setIfPresent(input::hasDestinationMessage, input::getDestinationMessage,
                updateRequest::setDestinationMessage, StringValue::getValue);
        ConverterUtils.setIfPresent(input::hasSource, input::getSource, updateRequest::setSource,
                destination -> AccountIdentifier
                .create(URI.create(destination.getValue())));
        ConverterUtils.setIfPresent(input::hasSourceMessage, input::getSourceMessage, updateRequest::setSourceMessage,
                StringValue::getValue);
        ConverterUtils.setIfPresent(input::hasDueDate, input::getDueDate, updateRequest::setDueDate,
                ProtobufModelUtils::timestampToDate);
        return updateRequest;
    }
}
