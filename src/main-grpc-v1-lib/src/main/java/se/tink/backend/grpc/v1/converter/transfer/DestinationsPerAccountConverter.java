package se.tink.backend.grpc.v1.converter.transfer;

import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.rpc.transfer.TransferDestinationsPerAccountResult;
import se.tink.grpc.v1.models.TransferDestinationPerAccount;

public class DestinationsPerAccountConverter implements Converter<TransferDestinationsPerAccountResult, TransferDestinationPerAccount> {

    private String currencyCode;

    public DestinationsPerAccountConverter(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    @Override
    public TransferDestinationPerAccount convertFrom(TransferDestinationsPerAccountResult input) {
        CoreTransferDestinationToGrpcTransferDestinationConverter converter = new CoreTransferDestinationToGrpcTransferDestinationConverter(currencyCode);
        return TransferDestinationPerAccount
                .newBuilder()
                .setAccountId(input.getAccountId())
                .addAllDestinations(
                        converter.convertFrom(input.getTransferDestinations())
                )
                .build();
    }
}
