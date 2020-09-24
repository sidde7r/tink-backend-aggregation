package se.tink.backend.aggregation.agents.nxgen.dk.banks.danskebank.rpc;

import static se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule.of;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.account.identifiers.DanishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class DkAccountEntity extends AccountEntity {

    private static final int ACCOUNT_NO_MIN_LENGTH = 10;

    @Override
    public CreditCardAccount toCreditCardAccount(DanskeBankConfiguration configuration) {
        return CreditCardAccount.builder(
                        getAccountNumberWithZerosIfIsTooShort(accountNoInt),
                        ExactCurrencyAmount.of(balance, currency),
                        calculateAvailableCredit())
                .setAccountNumber(getAccountNumberWithZerosIfIsTooShort(accountNoExt))
                .setName(accountName)
                .setBankIdentifier(getAccountNumberWithZerosIfIsTooShort(accountNoInt))
                .canExecuteExternalTransfer(
                        configuration.canExecuteExternalTransfer(accountProduct))
                .canReceiveExternalTransfer(
                        configuration.canReceiveExternalTransfer(accountProduct))
                .canPlaceFunds(configuration.canPlaceFunds(accountProduct))
                .canWithdrawCash(configuration.canWithdrawCash(accountProduct))
                .sourceInfo(createAccountSourceInfo())
                .build();
    }

    @Override
    public TransactionalAccount toCheckingAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withInferredAccountFlags()
                .withBalance(
                        BalanceModule.builder()
                                .withBalance(ExactCurrencyAmount.of(balance, currency))
                                .setAvailableCredit(calculateAvailableCredit())
                                .build())
                .withId(buildIdModule())
                .setBankIdentifier(getAccountNumberWithZerosIfIsTooShort(accountNoInt))
                .setApiIdentifier(getAccountNumberWithZerosIfIsTooShort(accountNoInt))
                .canExecuteExternalTransfer(AccountCapabilities.Answer.YES)
                .canReceiveExternalTransfer(AccountCapabilities.Answer.YES)
                .canPlaceFunds(AccountCapabilities.Answer.YES)
                .canWithdrawCash(AccountCapabilities.Answer.YES)
                .addAccountFlags(AccountFlag.PSD2_PAYMENT_ACCOUNT)
                .sourceInfo(createAccountSourceInfo())
                .build()
                .orElseThrow(IllegalStateException::new);
    }

    private IdModule buildIdModule() {
        return IdModule.builder()
                .withUniqueIdentifier(getAccountNumberWithZerosIfIsTooShort(accountNoInt))
                .withAccountNumber(getAccountNumberWithZerosIfIsTooShort(accountNoExt))
                .withAccountName(accountName)
                .addIdentifier(
                        new DanishIdentifier(getAccountNumberWithZerosIfIsTooShort(accountNoInt)))
                .build();
    }

    @Override
    protected TransactionalAccount toSavingsAccount(DanskeBankConfiguration configuration) {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.SAVINGS)
                .withInferredAccountFlags()
                .withBalance(of(ExactCurrencyAmount.of(balance, currency)))
                .withId(buildIdModule())
                .setBankIdentifier(getAccountNumberWithZerosIfIsTooShort(accountNoInt))
                .setApiIdentifier(getAccountNumberWithZerosIfIsTooShort(accountNoInt))
                .canExecuteExternalTransfer(
                        configuration.canExecuteExternalTransfer(accountProduct))
                .canReceiveExternalTransfer(
                        configuration.canReceiveExternalTransfer(accountProduct))
                .canPlaceFunds(configuration.canPlaceFunds(accountProduct))
                .canWithdrawCash(configuration.canWithdrawCash(accountProduct))
                .sourceInfo(createAccountSourceInfo())
                .build()
                .orElseThrow(IllegalStateException::new);
    }

    private String getAccountNumberWithZerosIfIsTooShort(String accountNumber) {
        return Strings.padStart(accountNumber, ACCOUNT_NO_MIN_LENGTH, '0');
    }
}
