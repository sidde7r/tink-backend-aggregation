package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.creditcards.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class CardAccountEntity {

    private String type;
    private boolean visible;
    private String accountId;
    private double statementBalanceAmount;
    private double amountAvailable;
    private String creditAccountIBAN;
    private CardEntity card;

    @JsonIgnore
    public boolean isActiveCreditCard() {
        return card != null
                && !card.isClosedCard()
                && OpBankConstants.AccountType.CREDIT_CARD.equalsIgnoreCase(card.getCardType());
    }

    @JsonIgnore
    public CreditCardAccount toTinkCreditCard() {
        return CreditCardAccount.builder(
                        creditAccountIBAN,
                        ExactCurrencyAmount.inEUR(statementBalanceAmount),
                        ExactCurrencyAmount.inEUR(amountAvailable))
                .setAccountNumber(card.getCardNumberMasked())
                .setName(getName())
                .setBankIdentifier(card.getEncryptedCardNumber())
                .build();
    }

    @JsonIgnore
    private String getName() {
        ProductEntity product = card.getProduct();
        if (product != null && !Strings.isNullOrEmpty(product.getName())) {
            return product.getName();
        }

        return card.getCardNumberMasked();
    }

    public String getType() {
        return type;
    }

    public boolean isVisible() {
        return visible;
    }

    public String getAccountId() {
        return accountId;
    }

    public double getStatementBalanceAmount() {
        return statementBalanceAmount;
    }

    public double getAmountAvailable() {
        return amountAvailable;
    }

    public String getCreditAccountIBAN() {
        return creditAccountIBAN;
    }

    public CardEntity getCard() {
        return card;
    }
}
