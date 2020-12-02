package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Preconditions;
import java.lang.invoke.MethodHandles;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.se.ClearingNumber;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountEntity implements GeneralAccountEntity {
    @JsonIgnore
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private String accountName;
    private String accountNumber;
    private String type;
    private double balance;
    private String bankName;
    private String clearingNumber;
    private double dispoibleAmount;
    private String ledger;
    private boolean localAccount;
    private String productCode;
    private boolean transferFrom;
    private boolean transferTo;
    private boolean youthAccount;

    public String getAccountName() {
        return accountName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getType() {
        return type;
    }

    public double getBalance() {
        return balance;
    }

    public String getBankName() {
        return bankName;
    }

    public String getClearingNumber() {
        return clearingNumber;
    }

    public double getDispoibleAmount() {
        return dispoibleAmount;
    }

    public String getLedger() {
        return ledger;
    }

    public String getProductCode() {
        return productCode;
    }

    public boolean isLocalAccount() {
        return localAccount;
    }

    public boolean isTransferFrom() {
        return transferFrom;
    }

    public boolean isTransferTo() {
        return transferTo;
    }

    public boolean isYouthAccount() {
        return youthAccount;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public void setClearingNumber(String clearingNumber) {
        this.clearingNumber = clearingNumber;
    }

    public void setDispoibleAmount(double dispoibleAmount) {
        this.dispoibleAmount = dispoibleAmount;
    }

    public void setLedger(String ledger) {
        this.ledger = ledger;
    }

    public void setLocalAccount(boolean localAccount) {
        this.localAccount = localAccount;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public void setTransferFrom(boolean transferFrom) {
        this.transferFrom = transferFrom;
    }

    public void setTransferTo(boolean transferTo) {
        this.transferTo = transferTo;
    }

    public void setYouthAccount(boolean youthAccount) {
        this.youthAccount = youthAccount;
    }

    public Account toAccount() {
        Account account = new Account();

        account.setBalance(balance);
        account.setAccountNumber(accountNumber);
        account.setBankId(accountNumber);
        account.setName(accountName);

        switch (type.toUpperCase()) {
            case "PENSION":
                account.setType(AccountTypes.PENSION);
                break;
            case "LOAN":
                account.setType(AccountTypes.LOAN);
                break;
            case "CREDIT_CARD":
                account.setType(AccountTypes.CREDIT_CARD);
                break;
            case "MORTGAGE":
                account.setType(AccountTypes.MORTGAGE);
                break;
            case "INVESTMENT":
                account.setType(AccountTypes.INVESTMENT);
                break;
            case "SAVINGS":
                account.setType(AccountTypes.SAVINGS);
                break;
            case "CHECKING":
                account.setType(AccountTypes.CHECKING);
                break;
            case "UNKNOWN":
                setTypeForAccountTypeUnknown(account);
                break;
            default:
                logger.info("unknown_account_type {}", SerializationUtils.serializeToString(this));
                account.setType(AccountTypes.OTHER);
                break;
        }

        // It seems that LF returns accountNumber as clearingNumber+accountNumber for LF's own
        // accounts
        account.putIdentifier(new SwedishIdentifier(accountNumber));

        if (type.equalsIgnoreCase("PENSION")) {
            Preconditions.checkState(
                    Preconditions.checkNotNull(account.getBankId()).matches("[0-9]{4}|[0-9]{11}"),
                    "Unexpected account.bankid '%s'. Reformatted?",
                    account.getBankId());
        } else {
            Preconditions.checkState(
                    Preconditions.checkNotNull(account.getBankId()).matches("[0-9]{11}"),
                    "Unexpected account.bankid '%s'. Reformatted?",
                    account.getBankId());
        }

        return account;
    }

    private void setTypeForAccountTypeUnknown(Account account) {
        // Our ambassador has confirmed that accounts called Aktielikvid doesn't hold investments,
        // only balance to buy stocks/funds with. Therefore map it as a savings account.
        if (StringUtils.containsIgnoreCase(accountName, "aktielikvid")) {
            account.setType(AccountTypes.SAVINGS);
        } else {
            logger.info(
                    "Account with type UNKNOWN and accountName {} mapped as OTHER", accountName);
            account.setType(AccountTypes.OTHER);
        }
    }

    /*
     * The methods below are for general purposes
     */

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {

        if (localAccount) {
            return new SwedishIdentifier(getAccountNumber());
        } else {
            // localAccount seems to mean my account (still need to check if local to LF)
            if (isLocalButNotUsersAccount()) {
                return new SwedishIdentifier(getAccountNumber());
            }

            return new SwedishIdentifier(getClearingNumber() + getAccountNumber());
        }
    }

    @JsonIgnore
    private boolean isLocalButNotUsersAccount() {
        if ("0".equals(clearingNumber) && accountNumber != null && accountNumber.length() > 4) {
            String cn = accountNumber.substring(0, 4);
            Optional<ClearingNumber.Details> details = ClearingNumber.get(cn);

            if (details.isPresent()) {
                return details.get().getBank() == ClearingNumber.Bank.LANSFORSAKRINGAR_BANK;
            }
        }

        return false;
    }

    @Override
    public String generalGetBank() {
        return getBankName();
    }

    @Override
    public String generalGetName() {
        return getAccountName();
    }
}
