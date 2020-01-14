package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class CardAccountEntity {

    private String currency;
    private String details;
    private String maskedPan;
    private String name;
    private String product;
    private String resourceId;
    private String status;
    private String usage;

    private CardCreditLimitEntity creditLimit;
    private CardBalanceEntity balance;
    private CardLinksEntity _links;

    @JsonIgnore
    public CreditCardAccount toCreditCardAccount() {

        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(name)
                                .withBalance(
                                        new ExactCurrencyAmount(
                                                new BigDecimal(
                                                        balance.getBalanceAmount().getAmount()),
                                                currency))
                                .withAvailableCredit(
                                        new ExactCurrencyAmount(
                                                new BigDecimal(
                                                        balance.getBalanceAmount().getAmount()),
                                                currency))
                                .withCardAlias(resourceId) // check this with live data
                                .build())
                .withPaymentAccountFlag()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(name)
                                .withAccountNumber(name)
                                .withAccountName(name)
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                Type.PAYMENT_CARD_NUMBER, maskedPan))
                                .build())
                .addHolderName(name)
                .build();
    }

    public boolean isCreditCardAccount() {
        return true;
    }
}
