package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.entities;

import com.google.common.base.Strings;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class SavingsAccountEntity {
    private String id;
    private String name;
    private double balance;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        return balance;
    }

    public ExactCurrencyAmount getAmount() {
        return ExactCurrencyAmount.inEUR(balance);
    }

    public boolean isValid() {
        return !Strings.isNullOrEmpty(id) && !Strings.isNullOrEmpty(name) && balance > 0;
    }

    public Optional<TransactionalAccount> toSavingsAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.SAVINGS)
                .withoutFlags()
                .withBalance(BalanceModule.of(getAmount()))
                .withId(buildId())
                .setApiIdentifier(id)
                .build();
    }

    private IdModule buildId() {
        return IdModule.builder()
                .withUniqueIdentifier(id)
                .withAccountNumber(id)
                .withAccountName(name)
                .addIdentifier(AccountIdentifier.create(AccountIdentifier.Type.TINK, id))
                .setProductName(name)
                .build();
    }
}
