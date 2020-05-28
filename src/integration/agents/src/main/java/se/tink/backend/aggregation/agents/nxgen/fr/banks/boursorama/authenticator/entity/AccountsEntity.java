package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.entity;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.BoursoramaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountsEntity {
    private String accountKey;
    private String accountNumber;
    private List<ActionsEntity> actions;
    private List<AccountsEntity> attachedProducts;
    private double balance;
    private String bankId;
    private String bankName;
    private String currency;
    private boolean excludeFromBalance;
    private String expirationDate;
    private List<String> flags;
    private String iban;
    private String label;
    private String number;
    private String referenceLabel;
    private String relationType;
    private String type;
    private String typeCategory;
    private String visualId;
    private String refreshDate;

    public BalanceModule getTinkBalance() {
        return BalanceModule.of(ExactCurrencyAmount.of(balance, currency));
    }

    public String getIban() {
        return iban;
    }

    public String getAccountName() {
        return label;
    }

    public String getAccountKey() {
        return accountKey;
    }

    public boolean isExternalAccount() {
        return flags.contains(BoursoramaConstants.AccountFlags.EXTERNAL_ACCOUNT_FLAG);
    }
}
