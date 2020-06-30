package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.entity;

import java.util.Date;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Getter
public class AccountTransactionEntity {

    private String id;
    private String type;
    private String status;
    private AmountEntity amount;
    private String description;
    private String tokenId;
    private String tokenTransferId;
    private Long createdAtMs;
    private Object providerTransactionDetails;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setDescription(description)
                .setAmount(ExactCurrencyAmount.of(amount.getValue(), amount.getCurrency()))
                .setDate(new Date(createdAtMs))
                .build();
    }
}
