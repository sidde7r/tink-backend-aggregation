package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.handelsbanken.fetcher.rpc;

import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.entity.TransactionsItemEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class FiTransactionsItemEntity extends TransactionsItemEntity {

    @Override
    public Transaction toTinkTransaction(String providerMarket) {
        return Transaction.builder()
                .setDate(getBookingDate())
                .setAmount(creditOrDebit())
                .setDescription(getRemittanceInformation())
                .setPending(false)
                .setPayload(
                        TransactionPayloadTypes.DETAILS,
                        SerializationUtils.serializeToString(getTransactionDetails()))
                .build();
    }

    @Override
    public Boolean hasDate() {
        return getBookingDate() != null;
    }
}
