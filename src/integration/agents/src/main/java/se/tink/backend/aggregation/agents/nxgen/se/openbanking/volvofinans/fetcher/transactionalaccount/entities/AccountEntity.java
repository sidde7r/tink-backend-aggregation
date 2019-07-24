package se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.fetcher.transactionalaccount.entities;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.VolvoFinansConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntity {

    private String resourceId;
    private String accountNumber;
    private String currency;
    private String product;
    private List<BalanceEntity> balances;
    private String accountType;
    private String status;

    public boolean isEnabled() {
        return status.equalsIgnoreCase(VolvoFinansConstants.Accounts.STATUS_ENABLED);
    }

    public TransactionalAccount toTinkAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withBalance(BalanceModule.of(getBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountNumber)
                                .withAccountNumber(accountNumber)
                                .withAccountName(product)
                                .addIdentifier(new SwedishIdentifier(accountNumber))
                                .build())
                .putInTemporaryStorage(VolvoFinansConstants.StorageKeys.ACCOUNT_ID, resourceId)
                .setApiIdentifier(resourceId)
                .setBankIdentifier(resourceId)
                .build();
    }

    private Amount getBalance() {
        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(this::isExpected)
                .findFirst()
                .map(BalanceEntity::getAmount)
                .orElse(getDefaultAmount());
    }

    private Amount getDefaultAmount() {
        return new Amount(currency, 0);
    }

    private boolean isExpected(final BalanceEntity balance) {
        return balance.isExpected();
    }

    public String getAccountNumber() {
        return resourceId;
    }
}
