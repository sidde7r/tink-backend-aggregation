package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction.Builder;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.chrono.AvailableDateInformation;

@JsonObject
public class BookedEntity {
    @Getter String transactionId;
    @Getter String creditorName;
    @Getter String creditorAccount;
    @Getter String debtorName;
    @Getter String debtorAccount;

    @JsonProperty("transactionAmount")
    @Getter
    TransactionAmountEntity transactionAccountEntity;

    @Getter LocalDate bookingDate;
    @Getter LocalDate valueDate;
    @Getter String bankTransactionCode;
    @Getter String remittanceInformationUnstructured;

    @JsonProperty("remittanceInformationStructured")
    @Getter
    RemittanceInformationStructuredEntity remittanceInformationStructuredEntity;

    @JsonIgnore
    public AggregationTransaction toTinkTransaction() {

        Builder builder =
                Transaction.builder()
                        .setAmount(getTransactionAccountEntity().toAmount())
                        .setDate(Optional.ofNullable(getBookingDate()).orElse(getValueDate()))
                        .setDescription(
                                Optional.ofNullable(getRemittanceInformationUnstructured())
                                        .orElse(
                                                getRemittanceInformationStructuredEntity()
                                                        .reference))
                        .setPending(false);
        /* .setTransactionDates(
        getTinkTransactionDates(getBookingDate(), getValueDate()));*/
        return (AggregationTransaction) builder.build();
    }

    public TransactionDates getTinkTransactionDates(LocalDate valueDate, LocalDate bookingDate) {
        TransactionDates.Builder builder = TransactionDates.builder();

        if (valueDate != null) {
            builder.setValueDate(new AvailableDateInformation().setDate(valueDate));
        }

        if (bookingDate != null) {
            builder.setBookingDate(new AvailableDateInformation().setDate(bookingDate));
        }

        return builder.build();
    }
}
