package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDateTime;
import java.time.ZoneId;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction.Builder;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateTimeDeserializer;
import se.tink.libraries.chrono.AvailableDateInformation;

@JsonObject
public class TransactionEntity {
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Stockholm");

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime bookingDate;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime valueDate;

    private AccountEntity creditorAccount;
    private AccountEntity debtorAccount;
    private String debtorName;
    private String entryReference;
    private String remittanceInformationUnstructured;
    private BalanceAmountEntity transactionAmount;
    private String transactionId;
    private String endToEndId;

    public Transaction toBookedTinkTransaction() {
        Builder builder =
                Transaction.builder()
                        .setDescription(remittanceInformationUnstructured)
                        .setDate(bookingDate.toLocalDate())
                        .setAmount(transactionAmount.toAmount())
                        .setPending(false)
                        .setTransactionDates(getTinkTransactionDates())
                        .addExternalSystemIds(
                                TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                                transactionId)
                        .setTransactionReference(endToEndId)
                        .setProviderMarket(SkandiaConstants.PROVIDER_MARKET);

        return (Transaction) builder.build();
    }

    private TransactionDates getTinkTransactionDates() {
        return TransactionDates.builder()
                .setValueDate(
                        new AvailableDateInformation()
                                .setInstant(valueDate.atZone(ZONE_ID).toInstant()))
                .setBookingDate(
                        new AvailableDateInformation()
                                .setInstant(bookingDate.atZone(ZONE_ID).toInstant()))
                .build();
    }
}
