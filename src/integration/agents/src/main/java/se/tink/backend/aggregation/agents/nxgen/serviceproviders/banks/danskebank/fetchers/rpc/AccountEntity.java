package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc;

import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.models.Loan.Type;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.source_info.AccountSourceInfo;
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Getter
public class AccountEntity {

    private String showCategory;
    private int sortValue;
    private boolean isFixedTermDeposit;
    private boolean isInLimitGroup;
    private boolean isSavingGoalAccountProduct;
    private boolean isBreadcrumbAccountProduct;
    private boolean isLoanAccount;
    private String invIdOwner;
    private String mandateAccMk;
    private boolean showAvailable;
    private boolean accessToCredit;
    private boolean accessToDebit;
    private boolean accessToQuery;
    private String currency;
    private String cardType;
    private String accountType;
    private String accountName;
    private String accountProduct;
    private String accountRegNoExt;
    private String accountNoExt;
    private String accountNoInt;
    private String languageCode;
    private double balanceAvailable;
    private double balance;

    public LoanAccount toLoanAccount(DanskeBankConfiguration configuration) {
        return LoanAccount.builder(accountNoInt, ExactCurrencyAmount.of(balance, currency))
                .setAccountNumber(accountNoExt)
                .setName(accountName)
                .setBankIdentifier(accountNoInt)
                .canExecuteExternalTransfer(
                        configuration.canExecuteExternalTransfer(accountProduct))
                .canReceiveExternalTransfer(
                        configuration.canReceiveExternalTransfer(accountProduct))
                .canPlaceFunds(configuration.canPlaceFunds(accountProduct))
                .canWithdrawCash(configuration.canWithdrawCash(accountProduct))
                .sourceInfo(createAccountSourceInfo())
                .setDetails(
                        isMortgage(configuration)
                                ? LoanDetails.builder(LoanDetails.Type.MORTGAGE)
                                        .setLoanNumber(accountNoExt)
                                        .build()
                                : null)
                .build();
    }

    private boolean isMortgage(final DanskeBankConfiguration configuration) {
        return configuration
                .getLoanAccountTypes()
                .getOrDefault(accountProduct, Type.OTHER)
                .equals(Type.MORTGAGE);
    }

    public CreditCardAccount toCreditCardAccount(DanskeBankConfiguration configuration) {
        return CreditCardAccount.builder(
                        accountNoInt,
                        ExactCurrencyAmount.of(balance, currency),
                        calculateAvailableCredit())
                .setAccountNumber(accountNoExt)
                .setName(accountName)
                .setBankIdentifier(accountNoInt)
                .canExecuteExternalTransfer(
                        configuration.canExecuteExternalTransfer(accountProduct))
                .canReceiveExternalTransfer(
                        configuration.canReceiveExternalTransfer(accountProduct))
                .canPlaceFunds(configuration.canPlaceFunds(accountProduct))
                .canWithdrawCash(configuration.canWithdrawCash(accountProduct))
                .sourceInfo(createAccountSourceInfo())
                .build();
    }

    public Optional<TransactionalAccount> toTransactionalAccount(TransactionalAccountType type) {
        return TransactionalAccount.nxBuilder()
                .withType(type)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(ExactCurrencyAmount.of(balance, currency)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountNoInt)
                                .withAccountNumber(accountNoExt)
                                .withAccountName(accountName)
                                .addIdentifier(new BbanIdentifier(accountNoExt))
                                .build())
                .setApiIdentifier(accountNoInt)
                .build();
    }

    private AccountSourceInfo createAccountSourceInfo() {
        return AccountSourceInfo.builder()
                .bankProductCode(accountProduct)
                .bankAccountType(accountType)
                .build();
    }

    private ExactCurrencyAmount calculateAvailableCredit() {
        return ExactCurrencyAmount.of(Math.max(balanceAvailable - balance, 0.0), currency);
    }
}
