package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.creditcard.rpc;

import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

@JsonObject
public class CardReservedTransactionEntity extends CardTransactionEntity {

    public Optional<CreditCardTransaction> toTinkCreditCardTransaction(
            CreditCardAccount creditCardAccount, String defaultCurrency) {

        validateCreditCardAndCurrency(creditCardAccount, defaultCurrency);

        if (getLocalAmount() == null) {
            return Optional.empty();
        }

        return Optional.of(
                CreditCardTransaction.builder()
                        .setAmount(getLocalAmount().toTinkAmount(defaultCurrency))
                        .setCreditCardAccountNumber(
                                creditCardAccount != null
                                        ? creditCardAccount.getAccountNumber()
                                        : null)
                        .setDate(getDate())
                        .setDescription(getDescription())
                        .setPending(true)
                        .build());
    }
}
