package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.fetcher.creditcardaccount.entities;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.SebConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.SebConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.fetcher.transactionalaccount.entities.BalancesEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntity {

    private String resourceId;
    private String maskedPan;
    private String currency;
    private String name;
    private String product;
    private List<BalancesEntity> balances;
    private AmountEntity creditLimit;
    private String status;
    private String usage;

    public CreditCardAccount toCreditCardAccount() {
        return CreditCardAccount.builder(resourceId)
                .setAccountNumber(resourceId)
                .setName(name)
                .setBalance(getAvailableBalance())
                .putInTemporaryStorage(StorageKeys.ACCOUNT_ID, resourceId)
                .build();
    }

    public boolean isEnabled() {
        return status.equalsIgnoreCase(SebConstants.Accounts.STATUS_ENABLED);
    }

    private Amount getAvailableBalance() {
        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(BalancesEntity::isAvailableBalance)
                .findFirst()
                .orElseThrow(IllegalStateException::new)
                .toAmount();
    }
}
