package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateDeserializer;
import se.tink.libraries.chrono.AvailableDateInformation;

@JsonObject
public class CardTransactionEntity {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date transactionDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date bookingDate;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate valueDate;

    private TransactionAmountEntity transactionAmount;

    private RemittanceInformationStructuredEntity remittanceInformationStructured;

    private String transactionDetails;

    private String transactionId;

    public Transaction toBookedTinkTransaction() {
        return toTinkTransaction(false);
    }

    public Transaction toPendingTinkTransaction() {
        return toTinkTransaction(true);
    }

    private Transaction toTinkTransaction(boolean isPending) {
        Date date = bookingDate != null ? bookingDate : transactionDate;
        TransactionDates transactionDates =
                TransactionDates.builder()
                        .setValueDate(new AvailableDateInformation(valueDate))
                        .build();

        return Transaction.builder()
                .addExternalSystemIds(
                        TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                        transactionId)
                .setPending(isPending)
                .setTransactionReference(getTransactionReference())
                .setTransactionDates(transactionDates)
                .setDescription(transactionDetails)
                .setAmount(transactionAmount.toAmount())
                .setDate(date)
                .build();
    }

    private String getTransactionReference() {
        return Optional.ofNullable(remittanceInformationStructured)
                .map(RemittanceInformationStructuredEntity::getReference)
                .orElse(null);
    }
}
