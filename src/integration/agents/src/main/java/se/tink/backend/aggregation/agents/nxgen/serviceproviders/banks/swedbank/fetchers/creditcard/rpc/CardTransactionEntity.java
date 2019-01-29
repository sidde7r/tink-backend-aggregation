package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.base.Preconditions;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

@JsonObject
public class CardTransactionEntity {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date date;
    private String description;
    private String expenseControlIncluded;
    private AmountEntity localAmount;

    public Date getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getExpenseControlIncluded() {
        return expenseControlIncluded;
    }

    public AmountEntity getLocalAmount() {
        return localAmount;
    }

    public Optional<CreditCardTransaction> toTinkCreditCardTransaction(CreditCardAccount creditCardAccount,
            String defaultCurrency) {
        Preconditions.checkArgument(creditCardAccount != null, "Credit card account cannot be null.");
        Preconditions.checkArgument(defaultCurrency != null, "Default currency cannot be null.");

        if (localAmount == null) {
            return Optional.empty();
        }

        return Optional.of(CreditCardTransaction.builder()
                .setAmount(localAmount.toTinkAmount(defaultCurrency))
                .setCreditAccount(creditCardAccount)
                .setDate(date)
                .setDescription(description)
                .build());
    }
}
