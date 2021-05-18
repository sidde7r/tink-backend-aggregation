package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.detail;

import java.math.BigDecimal;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail.AccountProductCode;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.BalancesDataEntity;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class TransactionalAccountMapper {

    public static Optional<TransactionalAccount> mapToTinkAccount(
            AccountEntity accountEntity, BalancesDataEntity balances) {
        IdModule id =
                IdModule.builder()
                        .withUniqueIdentifier(accountEntity.getIban())
                        .withAccountNumber(accountEntity.getIban())
                        .withAccountName(accountEntity.getName())
                        .addIdentifier(
                                AccountIdentifier.create(
                                        AccountIdentifierType.IBAN, accountEntity.getIban()))
                        .build();

        return TransactionalAccount.nxBuilder()
                .withType(getAccountType(accountEntity))
                .withInferredAccountFlags()
                .withBalance(buildBalanceModule(balances, accountEntity.getCurrency()))
                .withId(id)
                .setApiIdentifier(accountEntity.getAccountId())
                .build();
    }

    private static BalanceModule buildBalanceModule(BalancesDataEntity balances, String currency) {
        ExactCurrencyAmount accountBalance =
                ExactCurrencyAmount.of(
                        BigDecimal.valueOf(balances.getAccountBalance().getAmount()), currency);

        ExactCurrencyAmount availableBalance =
                ExactCurrencyAmount.of(
                        BigDecimal.valueOf(balances.getAvailableBalance().getAmount()), currency);

        ExactCurrencyAmount creditLimit =
                ExactCurrencyAmount.of(
                        BigDecimal.valueOf(balances.getAccountAvailableCredit().getAmount()),
                        currency);

        return BalanceModule.builder()
                .withBalance(accountBalance)
                .setAvailableBalance(availableBalance)
                .setCreditLimit(creditLimit)
                .build();
    }

    public static boolean isAccountOfInterest(AccountEntity accountEntity) {
        return AccountProductCode.isCheckingAccount(accountEntity)
                || AccountProductCode.isSavingsAccount(accountEntity);
    }

    private static TransactionalAccountType getAccountType(AccountEntity accountEntity) {
        if (AccountProductCode.isCheckingAccount(accountEntity)) {
            return TransactionalAccountType.CHECKING;
        } else if (AccountProductCode.isSavingsAccount(accountEntity)) {
            return TransactionalAccountType.SAVINGS;
        }
        return TransactionalAccountType.OTHER;
    }
}
