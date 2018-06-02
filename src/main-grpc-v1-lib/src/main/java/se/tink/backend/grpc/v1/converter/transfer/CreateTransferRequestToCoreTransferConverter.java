package se.tink.backend.grpc.v1.converter.transfer;

import java.net.URI;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.utils.NumberUtils;
import se.tink.backend.grpc.v1.utils.ProtobufModelUtils;
import se.tink.grpc.v1.rpc.CreateTransferRequest;
import se.tink.libraries.account.AccountIdentifier;

public class CreateTransferRequestToCoreTransferConverter implements Converter<CreateTransferRequest, Transfer> {
    @Override
    public Transfer convertFrom(CreateTransferRequest input) {
        Transfer transfer = new Transfer();
        ConverterUtils.setIfPresent(input::hasAmount, input::getAmount, transfer::setAmount, NumberUtils::toAmount);
        ConverterUtils.setIfPresent(input::getDestination, transfer::setDestination, destination -> AccountIdentifier
                .create(URI.create(destination)));
        ConverterUtils.setIfPresent(input::getDestinationMessage, transfer::setDestinationMessage);
        ConverterUtils.setIfPresent(input::getSource, transfer::setSource, source -> AccountIdentifier
                .create(URI.create(source)));
        ConverterUtils.setIfPresent(input::getSourceMessage, transfer::setSourceMessage);
        ConverterUtils.setIfPresent(input::hasDueDate, input::getDueDate, transfer::setDueDate,
                ProtobufModelUtils::timestampToDate);
        return transfer;
    }
}
