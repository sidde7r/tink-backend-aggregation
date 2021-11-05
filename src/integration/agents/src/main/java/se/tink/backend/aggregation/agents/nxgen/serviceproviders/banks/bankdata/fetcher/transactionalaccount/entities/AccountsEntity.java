package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.transactionalaccount.entities;

import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankDataConstants.Log;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankDataConstants.Storage;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities.Answer;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party.Role;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails.Type;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Getter
@Slf4j
public class AccountsEntity {
    private AccountNumberEntity accountNumber;
    private String name = "";
    private String ownerName;
    private boolean isOverdraft;
    private BalanceEntity balance;
    private BalanceEntity drawRight;
    private BalanceEntity disposalAmount;
    private BalanceEntity reservedAmount;
    private boolean showAmountAtDisposal;
    private boolean isOwnAccount;
    private boolean transfersFromAllowed;
    private boolean transfersToAllowed;
    private boolean isVisible;
    private boolean editNameAllowed;
    private String iban;
    private String swift;
    private String bankName;
    private boolean externalAccount;

    public Optional<TransactionalAccount> toTinkAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(getTinkAccountType())
                .withInferredAccountFlags()
                .withBalance(getBalanceModule())
                .withId(getTransactionalAccountIdModule())
                .addParties(new Party(ownerName, Role.HOLDER))
                .putInTemporaryStorage(Storage.PUBLIC_ID, accountNumber.getPublicId())
                .build();
    }

    public CreditCardAccount toTinkCreditCardAccount() {
        return CreditCardAccount.nxBuilder()
                .withCardDetails(getCardDetails())
                .withoutFlags()
                .withId(
                        getNonTransactionalAccountIdModule(
                                accountNumber.getRegNo() + accountNumber.getAccountNo()))
                .addHolderName(ownerName)
                .canWithdrawCash(AccountCapabilities.Answer.From(transfersFromAllowed))
                .putInTemporaryStorage(Storage.PUBLIC_ID, accountNumber.getPublicId())
                .build();
    }

    public InvestmentAccount toTinkPensionAccount() {
        return InvestmentAccount.nxBuilder()
                .withoutPortfolios()
                .withCashBalance(getBalanceObject())
                .withId(
                        getNonTransactionalAccountIdModule(
                                accountNumber.getRegNo() + accountNumber.getAccountNo()))
                .addHolderName(name)
                .canWithdrawCash(Answer.From(transfersFromAllowed))
                .build();
    }

    public LoanAccount toTinkLoanAccount() {
        return LoanAccount.nxBuilder()
                .withLoanDetails(
                        LoanModule.builder()
                                .withType(getLoanType())
                                .withBalance(getAvailableCredit())
                                .withInterestRate(
                                        0) // Mobile api does not seem to present interest rate
                                .build())
                .withId(getNonTransactionalAccountIdModule(iban))
                .build();
    }

    public boolean isTransactionalAccount() {
        return !isCreditCardAccount() && !isLoanAccount() && !isPensionAccount();
    }

    public boolean isCreditCardAccount() {
        return name.toLowerCase().contains("credit");
    }

    public boolean isLoanAccount() {
        return name.toLowerCase().contains("lån");
    }

    public boolean isPensionAccount() {
        return name.toLowerCase().contains("pension");
    }

    private TransactionalAccountType getTinkAccountType() {
        if (name.toLowerCase().contains("opsparing")) {
            return TransactionalAccountType.SAVINGS;
        }
        return TransactionalAccountType.CHECKING;
    }

    private BalanceModule getBalanceModule() {
        return BalanceModule.builder()
                .withBalance(getBalanceObject())
                .setAvailableBalance(getAvailableCredit())
                .build();
    }

    private IdModule getTransactionalAccountIdModule() {
        return IdModule.builder()
                .withUniqueIdentifier(iban)
                .withAccountNumber(accountNumber.getAccountNo())
                .withAccountName(name)
                .addIdentifier(getDKAccountIdentifier())
                .addIdentifier(new IbanIdentifier(iban))
                .build();
    }

    private IdModule getNonTransactionalAccountIdModule(String uniqueId) {
        return IdModule.builder()
                .withUniqueIdentifier(uniqueId)
                .withAccountNumber(accountNumber.getAccountNo())
                .withAccountName(name)
                .addIdentifier(getDKAccountIdentifier())
                .addIdentifier(new IbanIdentifier(iban))
                .build();
    }

    private AccountIdentifier getDKAccountIdentifier() {
        return AccountIdentifier.create(
                AccountIdentifierType.DK, accountNumber.getAccountNo(), name);
    }

    private CreditCardModule getCardDetails() {
        return CreditCardModule.builder()
                .withCardNumber(iban)
                .withBalance(getBalanceObject())
                .withAvailableCredit(getAvailableCredit())
                .withCardAlias(name)
                .build();
    }

    private ExactCurrencyAmount getAvailableCredit() {
        return ExactCurrencyAmount.of(disposalAmount.getAmount(), disposalAmount.getCurrencyCode());
    }

    private ExactCurrencyAmount getBalanceObject() {
        return ExactCurrencyAmount.of(balance.getAmount(), balance.getCurrencyCode());
    }

    private Type getLoanType() {
        if (name.toLowerCase().contains("bil")) {
            return Type.VEHICLE;
        }
        if (name.toLowerCase().contains("student")) {
            return Type.STUDENT;
        }
        log.info("tag={} Unknown loan type: {}", Log.UNKOWN_ACCOUNT_TYPE, name);
        return Type.OTHER;
    }
}
