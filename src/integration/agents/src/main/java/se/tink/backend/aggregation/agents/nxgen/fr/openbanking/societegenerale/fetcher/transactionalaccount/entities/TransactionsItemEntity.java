package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.entities;

import java.util.Date;
import java.util.Objects;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
@Data
@NoArgsConstructor
public class TransactionsItemEntity {

    private RemittanceInformationEntity remittanceInformation;

    private TransactionAmountEntity transactionAmount;

    private Date bookingDate;

    private CreditDebitIndicatorEntity creditDebitIndicator;

    private String entryReference;

    private String status;

    public Transaction toTinkTransactions() {
        return Transaction.builder()
                .setAmount(transactionAmount.getAmount(creditDebitIndicator))
                .setTransactionReference(entryReference)
                .setDate(bookingDate)
                .setDescription(
                        remittanceInformation.getUnstructured().stream()
                                .filter(Objects::nonNull)
                                .findFirst()
                                .orElse(creditDebitIndicator.toString()))
                .build();
    }
}
