package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.transaction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.agents.models.TransactionDateType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.common.TransactionAmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDate;
import se.tink.libraries.chrono.AvailableDateInformation;

@JsonObject
public class TransactionEntity {
    protected TransactionAmountEntity transactionAmount;
    protected String remittanceInformationUnstructured;
    protected String remittanceInformationStructured;

    public List<TransactionDate> getTinkTransactionDates(
            LocalDate valueDate, LocalDate bookingDate) {
        List<TransactionDate> transactionDates = new ArrayList<>();

        AvailableDateInformation valueDateInformation = new AvailableDateInformation();
        valueDateInformation.setDate(valueDate);

        transactionDates.add(
                TransactionDate.builder()
                        .type(TransactionDateType.VALUE_DATE)
                        .value(valueDateInformation)
                        .build());

        if (bookingDate != null) {
            AvailableDateInformation bookingDateInformation = new AvailableDateInformation();
            bookingDateInformation.setDate(bookingDate);
            transactionDates.add(
                    TransactionDate.builder()
                            .type(TransactionDateType.BOOKING_DATE)
                            .value(bookingDateInformation)
                            .build());
        }

        return transactionDates;
    }
}
