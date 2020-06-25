package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.entities;

import java.util.Date;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.utils.berlingroup.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TransactionEntity {

    private Date bookingDate;
    private String remittanceInformationUnstructured;
    private AmountEntity transactionAmount;

    Transaction toBookedTransaction() {
        return Transaction.builder()
                .setAmount(transactionAmount.toTinkAmount())
                .setDate(bookingDate)
                .setPending(false)
                .setDescription(remittanceInformationUnstructured)
                .build();
    }
}
