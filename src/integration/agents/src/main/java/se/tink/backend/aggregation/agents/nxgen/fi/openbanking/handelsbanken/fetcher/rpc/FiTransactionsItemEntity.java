package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.handelsbanken.fetcher.rpc;

import java.text.ParseException;
import java.time.DateTimeException;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.ExceptionMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.entity.TransactionsItemEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class FiTransactionsItemEntity extends TransactionsItemEntity {

    @Override
    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setDate(getDate())
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

    private Date getDate() {
        try {
            Optional<Date> date =
                    Optional.of(ThreadSafeDateFormat.FORMATTER_DAILY.parse(getBookingDate()));
            return date.orElseThrow(() -> new DateTimeException(ExceptionMessages.NOT_PARSE_DATE));
        } catch (ParseException e) {
            throw new DateTimeException(ExceptionMessages.NOT_PARSE_DATE);
        }
    }
}
