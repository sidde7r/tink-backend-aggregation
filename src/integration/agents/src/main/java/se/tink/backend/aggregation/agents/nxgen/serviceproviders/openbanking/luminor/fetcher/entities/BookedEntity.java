package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction.Builder;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.chrono.AvailableDateInformation;

@JsonObject
@Getter
public class BookedEntity {
    private String transactionId;
    private String creditorName;
    private IbanAccountEntity creditorAccount;

    private String debtorName;
    private IbanAccountEntity debtorAccount;

    private TransactionAmountEntity transactionAmount;

    private LocalDate bookingDate;
    private LocalDate valueDate;
    private String remittanceInformationUnstructured;

    private RemittanceInformationStructuredEntity remittanceInformationStructured;

    @JsonIgnore
    public Transaction toTinkTransaction() {
        Builder builder =
                Transaction.builder()
                        .setAmount(getTransactionAmount().toAmount())
                        .setDate(Optional.ofNullable(getBookingDate()).orElse(getValueDate()))
                        .setDescription(
                                Optional.ofNullable(getRemittanceInformationUnstructured())
                                        .orElse(
                                                getRemittanceInformationStructured()
                                                        .getReference()))
                        .setPending(false)
                        .setTransactionDates(getTinkTransactionDates())
                        .addExternalSystemIds(
                                TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                                transactionId)
                        .setMerchantName(creditorName)
                        .setProprietaryFinancialInstitutionType(
                                getRemittanceInformationStructured().getReferenceType())
                        .setTransactionReference(
                                getRemittanceInformationStructured().getReference());
        return (Transaction) builder.build();
    }

    public TransactionDates getTinkTransactionDates() {
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
