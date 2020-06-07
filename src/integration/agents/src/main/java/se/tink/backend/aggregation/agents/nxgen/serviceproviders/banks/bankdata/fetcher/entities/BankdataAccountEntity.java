package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataPaymentAccountCapabilities;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.account.enums.AccountFlag;
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
    private boolean transfersToAllowed;
    private boolean transfersFromAllowed;
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

    public TransactionalAccount toTinkAccount() {
        System.out.println(
                "!! Checking/Savings - name: "
                        + name
                        + ", "
                        + ", accountType: "
                        + accountType
                        + ", iban: "
                        + iban
                        + ", accountNo: "
                        + accountNo
                        + ", getType(): "
                        + getType());
        AccountTypes accountType = getType();
        return CheckingAccount.builder(
                        accountType,
                        constructUniqueIdentifier(),
                        ExactCurrencyAmount.of(balance, currencyCode))
                .setAccountNumber(iban)
                .setName(name)
                .setBankIdentifier(constructUniqueIdentifier())
                .putInTemporaryStorage(REGISTRATION_NUMBER_TEMP_STORAGE_KEY, regNo)
                .putInTemporaryStorage(ACCOUNT_NUMBER_TEMP_STORAGE_KEY, accountNo)
                .canMakeDomesticTransfer(
                        BankdataPaymentAccountCapabilities.canMakeDomesticTransfer(
                                name, accountType))
                .canReceiveDomesticTransfer(
                        BankdataPaymentAccountCapabilities.canReceiveDomesticTransfer(
                                name, accountType))
                .canWithdrawFunds(
                        BankdataPaymentAccountCapabilities.canWithdrawFunds(name, accountType))
                .canPlaceFunds(BankdataPaymentAccountCapabilities.canPlaceFunds(name, accountType))
                .addAccountFlag(AccountFlag.PSD2_PAYMENT_ACCOUNT)
                .addIdentifier(AccountIdentifier.create(Type.IBAN, iban))
                .build();
    }

    private AccountTypes getType() {
        return BankdataConstants.ACCOUNT_TYPE_MAPPER.translate(name).orElse(AccountTypes.CHECKING);
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

    public boolean isTransfersToAllowed() {
        return transfersToAllowed;
    }

    public boolean isTransfersFromAllowed() {
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
