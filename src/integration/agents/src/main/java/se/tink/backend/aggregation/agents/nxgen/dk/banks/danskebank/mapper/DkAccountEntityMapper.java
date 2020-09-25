package se.tink.backend.aggregation.agents.nxgen.dk.banks.danskebank.mapper;

import static se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule.of;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.mapper.AccountEntityMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountEntity;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.account.identifiers.DanishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class DkAccountEntityMapper extends AccountEntityMapper {

    private static final int ACCOUNT_NO_MIN_LENGTH = 10;

    @Override
    public CreditCardAccount toCreditCardAccount(
            DanskeBankConfiguration configuration, AccountEntity accountEntity) {
        return CreditCardAccount.builder(
                        getAccountNumberWithZerosIfIsTooShort(accountEntity.getAccountNoInt()),
                        ExactCurrencyAmount.of(
                                accountEntity.getBalance(), accountEntity.getCurrency()),
                        calculateAvailableCredit(accountEntity))
                .setAccountNumber(
                        getAccountNumberWithZerosIfIsTooShort(accountEntity.getAccountNoExt()))
                .setName(accountEntity.getAccountName())
                .setBankIdentifier(
                        getAccountNumberWithZerosIfIsTooShort(accountEntity.getAccountNoInt()))
                .canExecuteExternalTransfer(
                        configuration.canExecuteExternalTransfer(accountEntity.getAccountProduct()))
                .canReceiveExternalTransfer(
                        configuration.canReceiveExternalTransfer(accountEntity.getAccountProduct()))
                .canPlaceFunds(configuration.canPlaceFunds(accountEntity.getAccountProduct()))
                .canWithdrawCash(configuration.canWithdrawCash(accountEntity.getAccountProduct()))
                .sourceInfo(createAccountSourceInfo(accountEntity))
                .build();
    }

    @Override
    public Optional<TransactionalAccount> toCheckingAccount(AccountEntity accountEntity) {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withInferredAccountFlags()
                .withBalance(
                        BalanceModule.builder()
                                .withBalance(
                                        ExactCurrencyAmount.of(
                                                accountEntity.getBalance(),
                                                accountEntity.getCurrency()))
                                .setAvailableCredit(calculateAvailableCredit(accountEntity))
                                .build())
                .withId(buildIdModule(accountEntity))
                .setBankIdentifier(
                        getAccountNumberWithZerosIfIsTooShort(accountEntity.getAccountNoInt()))
                .setApiIdentifier(
                        getAccountNumberWithZerosIfIsTooShort(accountEntity.getAccountNoInt()))
                .canExecuteExternalTransfer(AccountCapabilities.Answer.YES)
                .canReceiveExternalTransfer(AccountCapabilities.Answer.YES)
                .canPlaceFunds(AccountCapabilities.Answer.YES)
                .canWithdrawCash(AccountCapabilities.Answer.YES)
                .addAccountFlags(AccountFlag.PSD2_PAYMENT_ACCOUNT)
                .sourceInfo(createAccountSourceInfo(accountEntity))
                .build();
    }

    private IdModule buildIdModule(AccountEntity accountEntity) {
        return IdModule.builder()
                .withUniqueIdentifier(
                        getAccountNumberWithZerosIfIsTooShort(accountEntity.getAccountNoInt()))
                .withAccountNumber(
                        getAccountNumberWithZerosIfIsTooShort(accountEntity.getAccountNoExt()))
                .withAccountName(accountEntity.getAccountName())
                .addIdentifier(
                        new DanishIdentifier(
                                getAccountNumberWithZerosIfIsTooShort(
                                        accountEntity.getAccountNoInt())))
                .build();
    }

    @Override
    protected Optional<TransactionalAccount> toSavingsAccount(
            DanskeBankConfiguration configuration, AccountEntity accountEntity) {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.SAVINGS)
                .withInferredAccountFlags()
                .withBalance(
                        of(
                                ExactCurrencyAmount.of(
                                        accountEntity.getBalance(), accountEntity.getCurrency())))
                .withId(buildIdModule(accountEntity))
                .setBankIdentifier(
                        getAccountNumberWithZerosIfIsTooShort(accountEntity.getAccountNoInt()))
                .setApiIdentifier(
                        getAccountNumberWithZerosIfIsTooShort(accountEntity.getAccountNoInt()))
                .canExecuteExternalTransfer(
                        configuration.canExecuteExternalTransfer(accountEntity.getAccountProduct()))
                .canReceiveExternalTransfer(
                        configuration.canReceiveExternalTransfer(accountEntity.getAccountProduct()))
                .canPlaceFunds(configuration.canPlaceFunds(accountEntity.getAccountProduct()))
                .canWithdrawCash(configuration.canWithdrawCash(accountEntity.getAccountProduct()))
                .sourceInfo(createAccountSourceInfo(accountEntity))
                .build();
    }

    private String getAccountNumberWithZerosIfIsTooShort(String accountNumber) {
        return Strings.padStart(accountNumber, ACCOUNT_NO_MIN_LENGTH, '0');
    }
}
