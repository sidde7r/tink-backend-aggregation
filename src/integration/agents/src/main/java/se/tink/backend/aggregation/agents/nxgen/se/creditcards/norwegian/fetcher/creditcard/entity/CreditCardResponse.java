package se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.fetcher.creditcard.entity;

import java.math.BigDecimal;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.NorwegianConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.identifiers.MaskedPanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class CreditCardResponse {

    private BigDecimal limit;
    private BigDecimal amountAvailable;
    private BigDecimal balance;
    private Link cardTransactionsLink;
    private Link paymentsLink;
    private Link invoiceLink;
    private Link limitLink;
    private Link pinLink;
    private Link blockCardLink;
    private Link myBuysLink;
    private Link disputeLink;

    private BigDecimal getBalance() {
        // include reserved transactions
        return amountAvailable.subtract(limit);
    }

    public CreditCardAccount toTinkAccount(CreditCardEntity cardEntity) {
        final String masked = cardEntity.getCardNumberMasked();

        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(masked)
                                .withBalance(
                                        ExactCurrencyAmount.of(
                                                getBalance(), NorwegianConstants.CURRENCY))
                                .withAvailableCredit(
                                        ExactCurrencyAmount.of(
                                                amountAvailable, NorwegianConstants.CURRENCY))
                                .withCardAlias(NorwegianConstants.CARD_ALIAS)
                                .build())
                .withoutFlags()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(NorwegianConstants.IDENTIFIER)
                                .withAccountNumber(masked)
                                .withAccountName(NorwegianConstants.CARD_ALIAS)
                                .addIdentifier(new MaskedPanIdentifier(masked))
                                .build())
                .build();
    }
}

@JsonObject
class Link {
    private String url;
    private String text;
}
