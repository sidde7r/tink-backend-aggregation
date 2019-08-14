package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.entity.account;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.Formats;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.entity.common.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {

    private String resourceId;
    private String iban;
    private String currency;
    private String product;
    private String name;
    private List<BalanceEntity> balances;
    private LinksEntity links;

    public Optional<TransactionalAccount> toTinkAccount() {

        final String nameTrimmed = name.substring(0, name.length() - 4);

        final AccountTypes type =
                FinecoBankConstants.ACCOUNT_TYPE_MAPPER
                        .translate(nameTrimmed)
                        .orElse(AccountTypes.OTHER);

        /*return type == (AccountTypes.CHECKING || AccountTypes.SAVINGS)
        ? Optional.ofNullable(toTypeAccount(type))
        : Optional.empty();*/

        switch (type) {
            case CHECKING:
                return Optional.ofNullable(toTypeAccount(TransactionalAccountType.CHECKING));
            case SAVINGS:
                return Optional.ofNullable(toTypeAccount(TransactionalAccountType.SAVINGS));
            default:
                return Optional.empty();
        }
    }

    private ExactCurrencyAmount getBalance() {
        return balances.stream()
                .filter(BalanceEntity::isForwardBalanceAvailable)
                .findAny()
                .map(balanceEntity -> balanceEntity.getBalanceAmount().toTinkAmount())
                .orElseGet(this::getInterimBalance);
    }

    private ExactCurrencyAmount getInterimBalance() {
        return balances.stream()
                .filter(BalanceEntity::isInterimBalanceAvailable)
                .findAny()
                .map(balanceEntity -> balanceEntity.getBalanceAmount().toTinkAmount())
                .orElse(new ExactCurrencyAmount(BigDecimal.ZERO, Formats.CURRENCY));
    }

    public TransactionalAccount toTypeAccount(TransactionalAccountType transactionalAccountType) {
        return TransactionalAccount.nxBuilder()
                .withType(transactionalAccountType)
                .withBalance(BalanceModule.of(getBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(iban)
                                .withAccountName(name)
                                .addIdentifier(AccountIdentifier.create(Type.IBAN, iban))
                                .build())
                .addHolderName(name)
                .setApiIdentifier(resourceId)
                .setBankIdentifier(iban)
                .putInTemporaryStorage(StorageKeys.ACCOUNT_ID, resourceId)
                .build();
    }
}
