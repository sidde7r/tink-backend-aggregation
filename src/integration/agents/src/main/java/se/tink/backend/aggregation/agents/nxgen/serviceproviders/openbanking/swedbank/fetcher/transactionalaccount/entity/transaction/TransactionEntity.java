package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.transaction;

import java.time.LocalDate;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.common.TransactionAmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.chrono.AvailableDateInformation;

@JsonObject
public class TransactionEntity {
    protected TransactionAmountEntity transactionAmount;
    protected String remittanceInformationUnstructured;
    protected String remittanceInformationStructured;

    public TransactionDates getTinkTransactionDates(LocalDate valueDate, LocalDate bookingDate) {
        TransactionDates.Builder builder = TransactionDates.builder();

        builder.setValueDate(new AvailableDateInformation().setDate(valueDate));

        if (bookingDate != null) {
            builder.setBookingDate(new AvailableDateInformation().setDate(bookingDate));
        }

        return builder.build();
    }
}
