package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.transactionalaccount.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

@JsonObject
public class CardTransactionEntity {

    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("transaction_date")
    private Date transactionDate;

    @JsonProperty("product_id")
    private String productId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("booked")
    private boolean booked;

    @JsonUnwrapped
    private AmountEntity amount;

    @JsonProperty("transaction_type")
    private String transactionType;

    public CreditCardTransaction toTinkTransaction(CreditCardAccount account) {

        return CreditCardTransaction.builder()
                .setAmount(amount)
                .setDate(transactionDate)
                .setPending(!booked)
                .setCreditAccount(account)
                .setDescription(title)
                .build();
    }
}
