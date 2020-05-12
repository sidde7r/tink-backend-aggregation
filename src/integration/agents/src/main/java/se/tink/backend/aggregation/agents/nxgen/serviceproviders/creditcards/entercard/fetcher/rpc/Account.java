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

@SuppressWarnings("unused")
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

        final Card card = getPrimaryCard(user);

        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(card.getMaskedNr())
                                .withBalance(
                                        ExactCurrencyAmount.of(
                                                usedCredit, EnterCardConstants.CURRENCY))
                                .withAvailableCredit(
                                        ExactCurrencyAmount.of(
                                                openToBuy, EnterCardConstants.CURRENCY))
                                .withCardAlias(card.getCardHolderName())
                                .build())
                .withInferredAccountFlags()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getKid())
                                .withAccountNumber(card.getMaskedNr())
                                .withAccountName(card.getCardHolderName())
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                Type.PAYMENT_CARD_NUMBER, getKid()))
                                .setProductName(productName)
                                .build())
                .setApiIdentifier(accountId)
                .build();
    }

    private Card getPrimaryCard(User user) {
        if (cards.isEmpty()) {
            return new Card(user.getName(), "****-****-****-****");
        }
        return cards.stream()
                .filter(Objects::nonNull)
                .filter(card -> card.isPrimary() == true)
                .findFirst()
                .orElse(cards.stream().findFirst().get());
    }

    public String getKid() {
        return kid.substring(0, kid.length() - 1);
    }
}
