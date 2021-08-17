package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.handelsbanken;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.ExceptionMessages;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.refresh.CheckingAccountRefreshException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.AccountsItemEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.BalancesItemEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class HandelsbankenAccountConverter implements HandelsbankenBaseAccountConverter {

    private static final int IBAN_COUNTRY_CODE_AND_CHECKSUM_LENGTH = 4;

    @Override
    public Optional<TransactionalAccount> toTinkAccount(
            AccountsItemEntity accountsItemEntity, AccountDetailsResponse accountDetails) {

        String iban = accountsItemEntity.getIban();

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(
                        BalanceModule.of(
                                getAmount(
                                        getBalancesItemEntity(accountsItemEntity, accountDetails))))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(iban)
                                .withAccountName(accountsItemEntity.getName())
                                .addIdentifier(
                                        new IbanIdentifier(accountsItemEntity.getBic(), iban))
                                .addIdentifier(
                                        new BbanIdentifier(
                                                iban.substring(
                                                        IBAN_COUNTRY_CODE_AND_CHECKSUM_LENGTH)))
                                .build())
                .addHolderName(accountDetails.getOwnerName())
                .setApiIdentifier(accountsItemEntity.getAccountId())
                .build();
    }

    private BalancesItemEntity getBalancesItemEntity(
            AccountsItemEntity accountsItemEntity, AccountDetailsResponse accountDetails) {
        return accountsItemEntity
                .getAvailableBalance(accountDetails)
                .orElseThrow(
                        () ->
                                new CheckingAccountRefreshException(
                                        ExceptionMessages.BALANCE_NOT_FOUND));
    }

    private ExactCurrencyAmount getAmount(BalancesItemEntity balance) {
        return new ExactCurrencyAmount(
                balance.getAmountEntity().getContent(), balance.getAmountEntity().getCurrency());
    }
}
