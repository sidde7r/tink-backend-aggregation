package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.handelsbanken;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.AccountsItemEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.BalancesItemEntity;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class HandelsbankenAccountConverter implements HandelsbankenBaseAccountConverter {

    @Override
    public Optional<TransactionalAccount> toTinkAccount(
            AccountsItemEntity accountsItemEntity, BalancesItemEntity balance) {

        String iban = accountsItemEntity.getIban();

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(getAmount(balance)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(iban)
                                .withAccountName(accountsItemEntity.getName())
                                .addIdentifier(
                                        AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                                .build())
                .addHolderName(accountsItemEntity.getName())
                .setApiIdentifier(accountsItemEntity.getAccountId())
                .build();
    }

    private ExactCurrencyAmount getAmount(BalancesItemEntity balance) {
        return new ExactCurrencyAmount(
                balance.getAmountEntity().getContent(), balance.getAmountEntity().getCurrency());
    }
}
