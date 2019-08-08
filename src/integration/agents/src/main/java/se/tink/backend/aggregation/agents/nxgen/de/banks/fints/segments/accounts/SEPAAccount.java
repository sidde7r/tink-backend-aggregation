package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.accounts;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import java.util.Collections;
import java.util.List;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.utils.FinTsAccountTypeConverter;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.strings.StringUtils;

public class SEPAAccount implements GeneralAccountEntity {

    private String iban;
    private String bic;
    private String accountNo;
    private String subAccount;
    private String blz;
    private String customerId;
    private int accountType;
    private String currency;
    private String accountOwner1;
    private String accountOwner2;
    private String productName;
    private String accountLimit;
    private String permittedBusinessTransactions;
    private String balance;
    private String bankName;
    private List<String> supportedSegments = Collections.emptyList();

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance, boolean isPositive) {
        if (!isPositive) {
            this.balance = "-" + balance;
        } else {
            this.balance = balance;
        }
    }

    public String getPermittedBusinessTransactions() {
        return permittedBusinessTransactions;
    }

    public void setPermittedBusinessTransactions(String permittedBusinessTransactions) {
        this.permittedBusinessTransactions = permittedBusinessTransactions;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public int getAccountType() {
        return accountType;
    }

    public void setAccountType(int accountType) {
        this.accountType = accountType;
    }

    public String getCurrency() {
        return Strings.isNullOrEmpty(currency) ? FinTsConstants.CURRENCY : currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getAccountOwner1() {
        return accountOwner1;
    }

    public void setAccountOwner1(String accountOwner1) {
        this.accountOwner1 = accountOwner1;
    }

    public String getAccountOwner2() {
        return accountOwner2;
    }

    public void setAccountOwner2(String accountOwner2) {
        this.accountOwner2 = accountOwner2;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getAccountLimit() {
        return accountLimit;
    }

    public void setAccountLimit(String accountLimit) {
        this.accountLimit = accountLimit;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getBic() {
        return bic;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getSubAccount() {
        return subAccount == null ? "" : subAccount;
    }

    public void setSubAccount(String subAccount) {
        this.subAccount = subAccount;
    }

    public String getBlz() {
        return blz;
    }

    public void setBlz(String blz) {
        this.blz = blz;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public List<String> getSupportedSegments() {
        return supportedSegments;
    }

    public void setSupportedSegments(List<String> supportedSegments) {
        this.supportedSegments = supportedSegments;
    }

    public TransactionalAccount toTinkAccount() {
        return TransactionalAccount.builder(
                        getType(),
                        getAccountNo(),
                        new Amount(getCurrency(), StringUtils.parseAmount(getBalance())))
                .setHolderName(new HolderName(getHolderName()))
                .setName(getProductName())
                .setAccountNumber(getAccountNo())
                .setBankIdentifier(getBlz() + getAccountNo())
                .addIdentifier(generalGetAccountIdentifier())
                .build();
    }

    private String getHolderName() {
        if (Strings.isNullOrEmpty(accountOwner2)) {
            return accountOwner1;
        } else {
            return accountOwner1 + ", " + accountOwner2;
        }
    }

    // Only consider transactional types for now
    private AccountTypes getType() {
        if (AccountTypes.CHECKING.equals(FinTsAccountTypeConverter.getAccountTypeFor(accountType))
                || AccountTypes.SAVINGS.equals(
                        FinTsAccountTypeConverter.getAccountTypeFor(accountType))) {
            return FinTsAccountTypeConverter.getAccountTypeFor(accountType);
        } else {
            throw new IllegalStateException("Invalid accountType for transactional account");
        }
    }

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        return AccountIdentifier.create(AccountIdentifier.Type.IBAN, getIban());
    }

    @Override
    public String generalGetBank() {
        return bankName;
    }

    @Override
    public String generalGetName() {
        return productName;
    }

    public boolean canMakeTransfer() {
        return supportedSegments.contains(FinTsConstants.Segments.HKCCS.name());
    }

    public boolean canMakePayment() {
        return supportedSegments.contains(FinTsConstants.Segments.HKCDE.name());
    }
}
