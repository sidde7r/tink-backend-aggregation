package se.tink.backend.aggregation.agents.banks.norwegian.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.regex.Pattern;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditCardEntity {
    private List<ActionLinksEntity> actionLinks;
    private String cardNumberMasked;
    private String expireDate;
    private int id;
    private boolean isActive;
    private String issueDate;

    public void setCardNumberMasked(String cardNumberMasked) {
        this.cardNumberMasked = cardNumberMasked;
    }

    @JsonIgnore
    public boolean hasValidBankId() {
        return Pattern.compile("[0-9]{6}[*]{6}[0-9]{4}").matcher(cardNumberMasked).matches();
    }

    @JsonIgnore
    public Account toTinkAccount(double balance, double availableCredit) {
        Account account = new Account();
        account.setBankId("NORWEGIAN_CARD");
        account.setName("Norwegiankortet");
        account.setBalance(balance);
        account.setAvailableCredit(availableCredit);
        account.setType(AccountTypes.CREDIT_CARD);
        account.setAccountNumber(cardNumberMasked);
        return account;
    }
}

@JsonObject
class ActionLinksEntity {
    private String svgIconSrc;
    private String title;
    private String url;
}
