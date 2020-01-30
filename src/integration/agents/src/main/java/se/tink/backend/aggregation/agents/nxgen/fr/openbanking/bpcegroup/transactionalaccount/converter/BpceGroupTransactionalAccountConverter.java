package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.converter;

import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.entity.accounts.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.entity.accounts.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.entity.accounts.BalanceType;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.entity.common.AmountEntity;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class BpceGroupTransactionalAccountConverter {

    public Optional<TransactionalAccount> toTransactionalAccount(
            AccountEntity accountEntity, List<BalanceEntity> balances) {
        final String iban = accountEntity.getIban();

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(getBalance(balances)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(getAccountNumber(iban))
                                .withAccountName(accountEntity.getName())
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .setApiIdentifier(accountEntity.getResourceId())
                .setBankIdentifier(accountEntity.getBicFi())
                .build();
    }

    private ExactCurrencyAmount getBalance(List<BalanceEntity> balances) {
        return findBalanceByType(balances, BalanceType.INSTANT)
                .map(Optional::of)
                .orElseGet(() -> findBalanceByType(balances, BalanceType.ACCOUNTING))
                .map(BalanceEntity::getBalanceAmount)
                .map(AmountEntity::toTinkAmount)
                .orElseThrow(() -> new IllegalStateException("No balance found"));
    }

    private static String getAccountNumber(String iban) {
        return iban.substring(iban.length() - 23);
    }

    private static Optional<BalanceEntity> findBalanceByType(
            List<BalanceEntity> balances, BalanceType type) {
        return balances.stream()
                .filter(b -> type.getType().equalsIgnoreCase(b.getBalanceType()))
                .findAny();
    }
}
