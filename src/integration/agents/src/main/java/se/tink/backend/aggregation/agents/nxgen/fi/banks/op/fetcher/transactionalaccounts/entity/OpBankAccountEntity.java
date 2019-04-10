package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.transactionalaccounts.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class OpBankAccountEntity {

    // Account related fields
    private String type;
    private String accountNumber;
    private String encryptedAccountNumber;
    private String bankingServiceTypeCode;
    private String versionNumber;
    private String productName;
    private String accountNameGivenByUser;
    private double amountAvailable;
    private String ownerName;
    private boolean mainAccountOfWebServiceAgreement;
    private double balance;
    private double netBalance;
    private double lineOfCredit;
    private double blocking;
    private String roleCode;
    private String startDateOfAuthorization;

    @JsonIgnore
    public boolean isTransactionalAccount() {
        return OpBankConstants.AccountType.ACCOUNT.equalsIgnoreCase(type);
    }

    @JsonIgnore
    public TransactionalAccount toTransactionalAccount() {
        return TransactionalAccount.builder(
                        getTinkAccountType(), accountNumber, Amount.inEUR(balance))
                .setAccountNumber(accountNumber)
                .setName(getAccountName())
                .setHolderName(new HolderName(ownerName))
                .addIdentifier(new IbanIdentifier(accountNumber))
                .setBankIdentifier(encryptedAccountNumber)
                .build();
    }

    @JsonIgnore
    public AccountTypes getTinkAccountType() {
        return OpBankConstants.ACCOUNT_TYPE_MAPPER
                .translate(bankingServiceTypeCode)
                .orElse(AccountTypes.OTHER);
    }

    @JsonIgnore
    private String getAccountName() {

        if (!Strings.isNullOrEmpty(accountNameGivenByUser)) {
            return accountNameGivenByUser;
        }

        if (!Strings.isNullOrEmpty(productName)) {
            return productName;
        }

        return accountNumber;
    }

    public String getType() {
        return type;
    }

    public OpBankAccountEntity setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
        return this;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getEncryptedAccountNumber() {
        return encryptedAccountNumber;
    }

    public OpBankAccountEntity setBankingServiceTypeCode(String bankingServiceTypeCode) {
        this.bankingServiceTypeCode = bankingServiceTypeCode;
        return this;
    }

    public String getBankingServiceTypeCode() {
        return bankingServiceTypeCode;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public String getProductName() {
        return productName;
    }

    public String getAccountNameGivenByUser() {
        return accountNameGivenByUser;
    }

    public double getAmountAvailable() {
        return amountAvailable;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public boolean isMainAccountOfWebServiceAgreement() {
        return mainAccountOfWebServiceAgreement;
    }

    public double getBalance() {
        return balance;
    }

    public double getNetBalance() {
        return netBalance;
    }

    public double getLineOfCredit() {
        return lineOfCredit;
    }

    public double getBlocking() {
        return blocking;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public String getStartDateOfAuthorization() {
        return startDateOfAuthorization;
    }
}
