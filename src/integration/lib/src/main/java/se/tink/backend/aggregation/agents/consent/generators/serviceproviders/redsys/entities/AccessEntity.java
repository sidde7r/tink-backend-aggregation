package se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessType;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@Setter
@Accessors(chain = true)
@JsonInclude(Include.NON_NULL)
public class AccessEntity {

    private List<AccountInfoEntity> accounts;

    private List<AccountInfoEntity> balances;

    private List<AccountInfoEntity> transactions;

    private AccessType availableAccounts;

    private AccessType availableAccountsWithBalances;

    private AccessType allPsd2;

    public static AccessEntity ofAllAccounts() {
        return new AccessEntity().setAvailableAccounts(AccessType.ALL_ACCOUNTS);
    }

    public static AccessEntity ofAllAccountsWithBalances() {
        return new AccessEntity().setAvailableAccountsWithBalances(AccessType.ALL_ACCOUNTS);
    }

    public static AccessEntity ofAllPsd2() {
        return new AccessEntity().setAllPsd2(AccessType.ALL_ACCOUNTS);
    }

    public static AccessEntity ofAccountWithBalancesAndTransactions(String iban) {
        List<AccountInfoEntity> accountInfoEntities =
                Collections.singletonList(new AccountInfoEntity(iban));
        return new AccessEntity()
                .setAccounts(accountInfoEntities)
                .setBalances(accountInfoEntities)
                .setTransactions(accountInfoEntities);
    }
}
