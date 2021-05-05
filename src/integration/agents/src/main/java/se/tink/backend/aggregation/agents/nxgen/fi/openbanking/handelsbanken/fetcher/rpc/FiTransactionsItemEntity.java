package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.handelsbanken.fetcher.rpc;

import java.util.Optional;
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
                .setAmount(getTinkAmount())
                .setDescription(getDescription())
                .setPending(false)
                .setPayload(
                        TransactionPayloadTypes.DETAILS,
                        SerializationUtils.serializeToString(getTinkTransactionDetails()))
                .build();
    }

    @Override
    public Boolean hasDate() {
        return getBookingDate() != null;
    }

    private String getDescription() {
        return Optional.ofNullable(getDebtorName()).orElse(getCreditorName());
    }
}
