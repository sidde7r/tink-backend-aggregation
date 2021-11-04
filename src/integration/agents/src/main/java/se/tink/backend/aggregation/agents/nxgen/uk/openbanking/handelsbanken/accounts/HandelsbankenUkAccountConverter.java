package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.handelsbanken.accounts;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.AccountsItemEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.handelsbanken.HandelsbankenConstants.AccountMapper;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
public class HandelsbankenUkAccountConverter implements HandelsbankenBaseAccountConverter {

    private final HandelsbankenUkBalanceMapper balanceMapper = new HandelsbankenUkBalanceMapper();

    @Override
    public Optional<TransactionalAccount> toTinkAccount(
            AccountsItemEntity accountEntity, AccountDetailsResponse accountDetails) {

        String accountName =
                Optional.ofNullable(accountEntity.getName()).orElse(accountEntity.getAccountType());

        AmountEntity amountEntity = balanceMapper.getBalance(accountDetails.getBalances());

        return TransactionalAccount.nxBuilder()
                .withType(getAccountType(accountEntity, accountDetails))
                .withPaymentAccountFlag()
                .withBalance(
                        BalanceModule.of(
                                ExactCurrencyAmount.of(
                                        amountEntity.getContent(), amountEntity.getCurrency())))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountEntity.getBbanWithoutClearing())
                                .withAccountNumber(accountEntity.getAccountNumberWithClearing())
                                .withAccountName(accountName)
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.IBAN,
                                                accountEntity.getIban(),
                                                accountName))
                                .build())
                .addHolderName(accountEntity.getOwnerName())
                .setApiIdentifier(accountEntity.getAccountId())
                .build();
    }

    private TransactionalAccountType getAccountType(
            AccountsItemEntity accountEntity, AccountDetailsResponse accountDetails) {

        if (isCurrentAccountBalance(accountEntity, accountDetails)) {
            return TransactionalAccountType.CHECKING;
        }
        if (isSavingsAccountBalance(accountEntity)) {
            return TransactionalAccountType.SAVINGS;
        }
        throw new IllegalStateException(
                String.format(
                        "[HandelsbankenAccountConverter] Cannot map to transactional account due to returned type is equal: %s",
                        accountEntity.getAccountType()));
    }

    private boolean isCurrentAccountBalance(
            AccountsItemEntity accountEntity, AccountDetailsResponse accountDetails) {
        return accountEntity.getAccountType().toLowerCase().contains(AccountMapper.CURRENT)
                || accountDetails.getBalances().stream()
                        .anyMatch(
                                balance ->
                                        balance.getBalanceType()
                                                .equalsIgnoreCase("AVAILABLE_AMOUNT"));
    }

    private boolean isSavingsAccountBalance(AccountsItemEntity accountEntity) {
        return accountEntity.getAccountType().toLowerCase().contains(AccountMapper.DEPOSIT);
    }
}
