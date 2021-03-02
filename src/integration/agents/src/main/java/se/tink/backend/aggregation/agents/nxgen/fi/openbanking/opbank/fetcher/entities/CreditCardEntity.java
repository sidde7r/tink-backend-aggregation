package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.entities;

import java.math.BigDecimal;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class CreditCardEntity {
    private String cardId;
    private String cardNumber;
    private String productName;
    private BigDecimal statementBalance;
    private BigDecimal authorizedOutstandingAmount;

    public CreditCardAccount toTinkAccount() {
        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(cardNumber)
                                .withBalance(new ExactCurrencyAmount(statementBalance, "EUR"))
                                .withAvailableCredit(
                                        new ExactCurrencyAmount(authorizedOutstandingAmount, "EUR"))
                                .withCardAlias(productName)
                                .build())
                .withInferredAccountFlags()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(cardId)
                                .withAccountNumber(cardNumber)
                                .withAccountName(productName)
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifier.Type.COUNTRY_SPECIFIC, cardId))
                                .build())
                .putInTemporaryStorage(OpBankConstants.StorageKeys.CARD_ID, cardId)
                .build();
    }
}
