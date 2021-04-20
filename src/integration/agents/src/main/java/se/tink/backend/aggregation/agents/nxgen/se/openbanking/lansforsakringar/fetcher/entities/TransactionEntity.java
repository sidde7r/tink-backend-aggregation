package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDate;
import lombok.Getter;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction.Builder;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateDeserializer;
import se.tink.libraries.chrono.AvailableDateInformation;

@JsonObject
@Getter
public class TransactionEntity {

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate bookingDate;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate transactionDate;

    private DebtorAccountEntity debtorAccount;
    private String entryReference;
    private String remittanceInformationUnstructured;
    private BalanceAmountEntity transactionAmount;
    private String merchantName;
    private String text;

    @JsonIgnore
    private String getTinkDescription() {
        if (remittanceInformationUnstructured != null) {
            return remittanceInformationUnstructured;
        }
        return merchantName != null ? merchantName : text;
    }

    @JsonIgnore
    public Transaction toTinkTransaction(boolean pending) {
        Builder builder =
                Transaction.builder()
                        .setAmount(transactionAmount.getAmount())
                        .setDate(transactionDate)
                        .setDescription(getTinkDescription())
                        .setPending(pending)
                        .setTransactionDates(getTinkTransactionDates(pending))
                        .addExternalSystemIds(
                                TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                                entryReference)
                        .setProprietaryFinancialInstitutionType(text)
                        .setMerchantName(merchantName)
                        .setProviderMarket(LansforsakringarConstants.PROVIDER_MARKET);

        return (Transaction) builder.build();
    }

    private TransactionDates getTinkTransactionDates(boolean pending) {
        TransactionDates.Builder builder = TransactionDates.builder();

        builder.setValueDate(new AvailableDateInformation().setDate(transactionDate));

        if (!pending) {
            builder.setBookingDate(new AvailableDateInformation().setDate(bookingDate));
        }

        return builder.build();
    }

    @JsonIgnore
    public Transaction toBookedTinkTransaction() {
        return toTinkTransaction(false);
    }

    @JsonIgnore
    public Transaction toPendinginkTransaction() {
        return toTinkTransaction(true);
    }
}
