package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.base.Preconditions;
import java.util.Date;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants.ErrorMessage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

@JsonObject
@Getter
public class CardTransactionEntity {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date date;

    private String description;
    private String expenseControlIncluded;
    private AmountEntity localAmount;

    public Optional<CreditCardTransaction> toTinkCreditCardTransaction(
            CreditCardAccount creditCardAccount, String defaultCurrency) {

        validateCreditCardAndCurrency(creditCardAccount, defaultCurrency);

        if (localAmount == null) {
            return Optional.empty();
        }

        return Optional.of(
                CreditCardTransaction.builder()
                        .setAmount(localAmount.toTinkAmount(defaultCurrency))
                        .setCreditAccount(
                                creditCardAccount != null
                                        ? creditCardAccount.getAccountNumber()
                                        : null)
                        .setDate(date)
                        .setDescription(description)
                        .build());
    }

    protected void validateCreditCardAndCurrency(
            CreditCardAccount creditCardAccount, String defaultCurrency) {
        Preconditions.checkArgument(creditCardAccount != null, ErrorMessage.NO_CREDIT_CARD_ACCOUNT);
        Preconditions.checkArgument(defaultCurrency != null, ErrorMessage.DEFAULT_CURRENCY_NULL);
    }
}
