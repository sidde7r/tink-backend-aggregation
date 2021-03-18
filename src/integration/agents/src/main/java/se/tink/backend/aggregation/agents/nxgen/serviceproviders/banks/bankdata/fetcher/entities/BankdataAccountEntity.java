package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataPaymentAccountCapabilities;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BankdataAccountEntity {

    public static final String REGISTRATION_NUMBER_TEMP_STORAGE_KEY = "regNo";
    public static final String ACCOUNT_NUMBER_TEMP_STORAGE_KEY = "accountNo";

    private String regNo;
    private String accountNo;
    private int accountType;
    private double balance;
    private String bicSwift;
    private String currencyCode;
    private double drawingRight;
    private String iban;
    private String name;
    private boolean overdraft;
    private Boolean transfersToAllowed;
    private Boolean transfersFromAllowed;
    private boolean editNameAllowed;
    private double yearToDayPayout;
    private double yearToDayDeposit;
    private String accountOwner;
    private boolean allShadowAccountsInactive;
    private boolean ownAccount;
    private double amountAtDisposal;
    private boolean amountAtDisposalToBeShown;
    private double totalReservation;
    private double totalShadowAccountBalance;
    private int priority;
    private boolean mastercard;
    private int regNoAsInt;
    private long accountNoAsLong;
    private long accountOwnerRefNo;

    public Optional<TransactionalAccount> toTinkAccount() {
        AccountTypes type = getType();
        TransactionalAccountType transType = TransactionalAccountType.from(type).orElse(null);
        if (!isProperTransactionalAccount(transType)) {
            return Optional.empty();
        }
        return TransactionalAccount.nxBuilder()
                .withType(transType)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(ExactCurrencyAmount.of(getBalance(), currencyCode)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(constructUniqueIdentifier())
                                .withAccountNumber(accountNo)
                                .withAccountName(name)
                                .addIdentifier(
                                        AccountIdentifier.create(AccountIdentifierType.IBAN, iban))
                                .build())
                .addHolderName(accountOwner)
                .setApiIdentifier(constructUniqueIdentifier())
                .putInTemporaryStorage(REGISTRATION_NUMBER_TEMP_STORAGE_KEY, regNo)
                .putInTemporaryStorage(ACCOUNT_NUMBER_TEMP_STORAGE_KEY, accountNo)
                .canExecuteExternalTransfer(
                        BankdataPaymentAccountCapabilities.canExecuteExternalTransfer(
                                name, type, this))
                .canReceiveExternalTransfer(
                        BankdataPaymentAccountCapabilities.canReceiveExternalTransfer(
                                name, type, this))
                .canWithdrawCash(BankdataPaymentAccountCapabilities.canWithdrawCash(name, type))
                .canPlaceFunds(BankdataPaymentAccountCapabilities.canPlaceFunds(name, type, this))
                .build();
    }

    private boolean isProperTransactionalAccount(TransactionalAccountType type) {
        return type != null && type != TransactionalAccountType.OTHER;
    }

    public AccountTypes getType() {
        final String savingsPartialName = "Opsparing";
        AccountTypes accountTypes;
        if (StringUtils.containsIgnoreCase(name, savingsPartialName)) {
            accountTypes = AccountTypes.SAVINGS;
        } else if (drawingRight > 0) {
            accountTypes = AccountTypes.LOAN;
        } else {
            accountTypes = AccountTypes.CHECKING;
        }
        return accountTypes;
    }

    private String constructUniqueIdentifier() {
        return iban;
    }

    public String getRegNo() {
        return regNo;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public int getAccountType() {
        return accountType;
    }

    public double getBalance() {
        return balance;
    }

    public String getBicSwift() {
        return bicSwift;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public double getDrawingRight() {
        return drawingRight;
    }

    public String getIban() {
        return iban;
    }

    public String getName() {
        return name;
    }

    public boolean isOverdraft() {
        return overdraft;
    }

    public Boolean isTransfersToAllowed() {
        return transfersToAllowed;
    }

    public Boolean isTransfersFromAllowed() {
        return transfersFromAllowed;
    }

    public boolean isEditNameAllowed() {
        return editNameAllowed;
    }

    public double getYearToDayPayout() {
        return yearToDayPayout;
    }

    public double getYearToDayDeposit() {
        return yearToDayDeposit;
    }

    public String getAccountOwner() {
        return accountOwner;
    }

    public boolean isAllShadowAccountsInactive() {
        return allShadowAccountsInactive;
    }

    public boolean isOwnAccount() {
        return ownAccount;
    }

    public double getAmountAtDisposal() {
        return amountAtDisposal;
    }

    public boolean isAmountAtDisposalToBeShown() {
        return amountAtDisposalToBeShown;
    }

    public double getTotalReservation() {
        return totalReservation;
    }

    public double getTotalShadowAccountBalance() {
        return totalShadowAccountBalance;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isMastercard() {
        return mastercard;
    }

    public int getRegNoAsInt() {
        return regNoAsInt;
    }

    public long getAccountNoAsLong() {
        return accountNoAsLong;
    }

    public long getAccountOwnerRefNo() {
        return accountOwnerRefNo;
    }
}
