package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.BoursoramaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Getter
public class AccountsEntity {
    private String accountKey;
    private double balance;
    private String currency;
    private List<String> flags;
    private String iban;

    @JsonProperty("label")
    private String accountName;

    public BalanceModule getTinkBalance() {
        return BalanceModule.of(ExactCurrencyAmount.of(balance, currency));
    }

    public boolean isExternalAccount() {
        return flags.contains(BoursoramaConstants.AccountFlags.EXTERNAL_ACCOUNT_FLAG);
    }
}
