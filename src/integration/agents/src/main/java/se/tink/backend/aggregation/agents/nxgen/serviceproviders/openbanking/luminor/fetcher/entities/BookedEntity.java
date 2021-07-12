package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    String transactionId;
    String creditorName;
    IbanAccountEntity creditorAccount;

    @Getter String debtorName;
    IbanAccountEntity debtorAccount;

    @JsonProperty("transactionAmount")
    TransactionAmountEntity transactionAccountEntity;

    LocalDate bookingDate;
    LocalDate valueDate;
    String bankTransactionCode;
    String remittanceInformationUnstructured;

    @JsonProperty("remittanceInformationStructured")
    RemittanceInformationStructuredEntity remittanceInformationStructuredEntity;

    @JsonIgnore
    public Transaction toTinkTransaction() {
        Builder builder =
                Transaction.builder()
                        .setAmount(getTransactionAccountEntity().toAmount())
                        .setDate(Optional.ofNullable(getBookingDate()).orElse(getValueDate()))
                        .setDescription(
                                Optional.ofNullable(getRemittanceInformationUnstructured())
                                        .orElse(
                                                getRemittanceInformationStructuredEntity()
                                                        .reference))
                        .setPending(false)
                        .setTransactionDates(getTinkTransactionDates())
                        .addExternalSystemIds(
                                TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                                transactionId)
                        .setMerchantName(creditorName)
                        .setProprietaryFinancialInstitutionType(
                                getRemittanceInformationStructuredEntity().getReferenceType())
                        .setTransactionReference(
                                getRemittanceInformationStructuredEntity().getReference());
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
