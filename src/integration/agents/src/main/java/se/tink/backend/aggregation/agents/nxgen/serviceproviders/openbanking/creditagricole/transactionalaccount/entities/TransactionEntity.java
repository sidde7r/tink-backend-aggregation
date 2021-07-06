package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants.BookingStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateDeserializer;
import se.tink.libraries.chrono.AvailableDateInformation;

@JsonObject
public class TransactionEntity {
    private Date bookingDate;
    private CreditDebitIndicatorEntity creditDebitIndicator;
    private String entryReference;
    private List<String> remittanceInformation;
    private String resourceId;
    private String status;
    private AmountEntity transactionAmount;
    private Date transactionDate;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate valueDate;

    public Transaction toTinkTransaction() {
        final boolean pending = status.equalsIgnoreCase(BookingStatus.PENDING);
        TransactionDates transactionDates =
                TransactionDates.builder()
                        .setValueDate(new AvailableDateInformation().setDate(valueDate))
                        .build();

        return Transaction.builder()
                .setTransactionDates(transactionDates)
                .setTransactionReference(entryReference)
                .addExternalSystemIds(
                        TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID, resourceId)
                .setPending(pending)
                .setAmount(transactionAmount.toAmount(creditDebitIndicator))
                .setDate(bookingDate)
                .setDescription(String.join(", ", remittanceInformation))
                .build();
    }
}
