package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.creditcard.rpc;

import com.google.common.base.Preconditions;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

@JsonObject
public class CardReservedTransactionEntity extends  CardTransactionEntity {

    public Optional<CreditCardTransaction> toTinkCreditCardTransaction(CreditCardAccount creditCardAccount,
            String defaultCurrency) {
        Preconditions.checkArgument(creditCardAccount != null, "Credit card account cannot be null.");
        Preconditions.checkArgument(defaultCurrency != null, "Default currency cannot be null.");

        if (getLocalAmount() == null) {
            return Optional.empty();
        }

        return Optional.of(CreditCardTransaction.builder()
                .setAmount(getLocalAmount().toTinkAmount(defaultCurrency))
                .setCreditAccount(creditCardAccount)
                .setDate(getDate())
                .setDescription(getDescription())
                .setPending(true)
                .build());
    }
}
