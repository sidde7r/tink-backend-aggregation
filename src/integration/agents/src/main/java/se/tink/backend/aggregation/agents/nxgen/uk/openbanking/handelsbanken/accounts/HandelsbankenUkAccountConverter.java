package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.handelsbanken.accounts;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.AccountsItemEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.handelsbanken.HandelsbankenConstants.AccountMapper;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;

@Slf4j
public class HandelsbankenUkAccountConverter implements HandelsbankenBaseAccountConverter {

    private final HandelsbankenUkBalanceMapper balanceMapper = new HandelsbankenUkBalanceMapper();

    @Override
    public Optional<TransactionalAccount> toTinkAccount(
            AccountsItemEntity accountEntity, AccountDetailsResponse accountDetails) {

        String accountName =
                Optional.ofNullable(accountEntity.getName()).orElse(accountEntity.getAccountType());

        return TransactionalAccount.nxBuilder()
                .withType(getAccountType(accountEntity, accountDetails))
                .withPaymentAccountFlag()
                .withBalance(balanceMapper.createAccountBalance(accountDetails.getBalances()))
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
        if (isCurrent(accountEntity, accountDetails)) {
            return TransactionalAccountType.CHECKING;
        }
        if (isSavings(accountEntity, accountDetails)) {
            return TransactionalAccountType.SAVINGS;
        }
        throw new IllegalStateException(
                String.format(
                        "[HandelsbankenAccountConverter] Cannot map to transactional account due to returned type is equal: %s",
                        accountEntity.getAccountType()));
    }

    private boolean isCurrent(AccountsItemEntity account, AccountDetailsResponse accountDetails) {
        return account.getAccountType().toLowerCase().contains(AccountMapper.CURRENT)
                || account.getAvailableBalance(accountDetails).isPresent();
    }

    private boolean isSavings(AccountsItemEntity account, AccountDetailsResponse accountDetails) {
        return account.getAccountType().toLowerCase().contains(AccountMapper.DEPOSIT)
                || account.getCurrentBalance(accountDetails).isPresent();
    }
}
