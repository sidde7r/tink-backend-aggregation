package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.fetcher.rpc;

import java.util.List;
import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.EnterCardConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class Account {

    private String productName;
    private double creditLimit;
    private double usedCredit;
    private double openToBuy;
    private boolean displayEFaktura;
    private boolean enableChangeDueDate;
    private List<Card> cards;
    private Object eFakturaMsg;
    private Object autoPaymentMsg;
    private String kID;
    private String defaultPayableAmount;
    private String kid;

    public CreditCardAccount toCreditCardAccount(User user, String accountId) {

        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(getPrimaryCard().getMaskedNr())
                                .withBalance(
                                        ExactCurrencyAmount.of(
                                                usedCredit, EnterCardConstants.CURRENCY))
                                .withAvailableCredit(
                                        ExactCurrencyAmount.of(
                                                openToBuy, EnterCardConstants.CURRENCY))
                                .withCardAlias(getPrimaryCard().getCardHolderName())
                                .build())
                .withInferredAccountFlags()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountId)
                                .withAccountNumber(getPrimaryCard().getMaskedNr())
                                .withAccountName(getPrimaryCard().getCardHolderName())
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                Type.PAYMENT_CARD_NUMBER, getKid()))
                                .setProductName(productName)
                                .build())
                .setApiIdentifier(accountId)
                .build();
    }

    private Card getPrimaryCard() {
        return cards.stream().filter(Objects::nonNull).filter(Card::isPrimary).findFirst().get();
    }

    public String getKid() {
        return kid.substring(0, kid.length() - 1);
    }
}
