package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount.entity.account;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.Amount;

import java.util.List;
import java.util.Optional;

@JsonObject
public class AccountEntity {

    private List<BalanceEntity> balances;
    private String bban;
    private String bic;
    private String currency;
    private String iban;
    private String name;
    private List<String> owner;
    private String resourceId;

    public TransactionalAccount toTinkModel() {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(bban)
                                .withAccountNumber(bban)
                                .withAccountName(getOwner())
                                .addIdentifier(
                                        AccountIdentifier.create(AccountIdentifier.Type.SE, bban))
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .withBalance(BalanceModule.of(getBalance()))
                .setApiIdentifier(resourceId)
                .addHolderName(getOwner())
                .build();
    }

    private String getOwner() {
        return Optional.ofNullable(owner).flatMap(owners -> owners.stream().findFirst()).orElse("");
    }

    private Amount getBalance() {
        return this.balances.stream()
                .filter(BalanceEntity::isInterimAvailable)
                .findFirst()
                .map(BalanceEntity::getAmount)
                .orElseGet(() -> new Amount(currency, 0));
    }
}
