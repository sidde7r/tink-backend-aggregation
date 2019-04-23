package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.fetcher.rpc;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.libraries.amount.Amount;

@SuppressWarnings("unused")
@JsonObject
public class Account {

    private String productName;
    private double creditLimit;
    private double usedCredit;
    private double openToBuy;
    private boolean displayEFaktura;
    private boolean enableChangeDueDate;
    private List<Card> cards = null;
    private Object eFakturaMsg;
    private Object autoPaymentMsg;
    private String kID;
    private String defaultPayableAmount;
    private String kid;

    public CreditCardAccount toCreditCardAccount(User user, String accountId) {
        return CreditCardAccount.builder(accountId)
                .setHolderName(new HolderName(user.name))
                .setBankIdentifier(accountId)
                .setAvailableCredit(Amount.inSEK(openToBuy))
                .setAccountNumber(accountId)
                .setBalance(Amount.inSEK(usedCredit).negate())
                .setName(productName)
                .build();
    }
}
