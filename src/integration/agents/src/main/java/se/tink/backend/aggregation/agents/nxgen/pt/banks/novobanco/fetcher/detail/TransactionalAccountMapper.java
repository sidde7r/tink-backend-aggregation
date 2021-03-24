package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.detail;

import java.util.Optional;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.pair.Pair;

public class TransactionalAccountMapper {

    public static Optional<TransactionalAccount> mapToTinkAccount(
            String internalAccountId, String iban, String name, double balance, String currency) {
        BalanceModule balanceModule =
                BalanceModule.builder()
                        .withBalance(ExactCurrencyAmount.of(balance, currency))
                        .build();

        Pair<String, AccountIdentifierType> accountId =
                AccountIdentifierProvider.getAccountIdentifierData(internalAccountId, iban);

        IdModule idModule =
                IdModule.builder()
                        .withUniqueIdentifier(accountId.first)
                        .withAccountNumber(internalAccountId)
                        .withAccountName(name)
                        .addIdentifier(AccountIdentifier.create(accountId.second, accountId.first))
                        .build();

        return TransactionalAccount.nxBuilder()
                .withType(getAccountType(iban))
                .withoutFlags()
                .withBalance(balanceModule)
                .withId(idModule)
                .build();
    }

    private static TransactionalAccountType getAccountType(String iban) {
        return Optional.ofNullable(iban)
                .map(s -> TransactionalAccountType.CHECKING)
                .orElse(TransactionalAccountType.SAVINGS);
    }
}
