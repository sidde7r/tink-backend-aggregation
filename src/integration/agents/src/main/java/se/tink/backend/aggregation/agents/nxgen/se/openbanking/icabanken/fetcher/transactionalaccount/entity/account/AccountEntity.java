package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount.entity.account;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

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

    public Optional<TransactionalAccount> toTinkModel() {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(getBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(bban)
                                .withAccountNumber(bban)
                                .withAccountName(name)
                                .addIdentifier(
                                        AccountIdentifier.create(AccountIdentifierType.SE, bban))
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .setApiIdentifier(resourceId)
                .addHolderName(getOwner())
                .build();
    }

    private String getOwner() {
        return Optional.ofNullable(owner).flatMap(owners -> owners.stream().findFirst()).orElse("");
    }

    private ExactCurrencyAmount getBalance() {
        return this.balances.stream()
                .filter(BalanceEntity::isInterimAvailable)
                .findFirst()
                .map(BalanceEntity::getAmount)
                .orElseGet(() -> new ExactCurrencyAmount(BigDecimal.ZERO, currency));
    }
}
