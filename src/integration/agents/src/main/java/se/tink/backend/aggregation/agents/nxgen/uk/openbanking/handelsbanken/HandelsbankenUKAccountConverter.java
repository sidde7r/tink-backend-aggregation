package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.handelsbanken;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.refresh.AccountRefreshException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.AccountsItemEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.BalancesItemEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.handelsbanken.HandelsbankenConstants.ExceptionMessages;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;

@Slf4j
public class HandelsbankenUKAccountConverter implements HandelsbankenBaseAccountConverter {

    @Override
    public Optional<TransactionalAccount> toTinkAccount(
            AccountsItemEntity accountEntity, AccountDetailsResponse accountDetails) {

        Optional<BalancesItemEntity> availableBalance =
                accountEntity.getAvailableBalance(accountDetails);
        if (!availableBalance.isPresent()) {
            throw new AccountRefreshException(ExceptionMessages.AVAILABLE_BALANCE_NOT_FOUND);
        }
        String accountName =
                Optional.ofNullable(accountEntity.getName()).orElse(accountEntity.getAccountType());

        return TransactionalAccount.nxBuilder()
                .withType(getAccountType(accountEntity))
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(accountEntity.getAmount(availableBalance.get())))
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

    private TransactionalAccountType getAccountType(AccountsItemEntity accountEntity) {
        TypeMapper<TransactionalAccountType> accountTypes =
                TypeMapper.<TransactionalAccountType>builder()
                        .put(TransactionalAccountType.CHECKING, "Current Account")
                        .put(TransactionalAccountType.SAVINGS, "Instant Access Deposit Account")
                        .build();

        log.info(
                "[HandelsbankenAccountConverter] Account type is: {}",
                accountEntity.getAccountType());
        return accountTypes
                .translate(accountEntity.getAccountType())
                .orElse(
                        TransactionalAccountType
                                .CHECKING); // temporary fallback for missing AccountType
    }
}
