package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.rpc;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountDetailsResponse {
    private int grantedOverdraft;
    private String grantedOverdraftTxt;
    private String grantedOverdraftDueDate;
    private boolean nemKonto;
    private boolean stdAccount;
    private String iban;
    private String accountType;
    private String accountName;
    private String accountId;
    private String accountHolder;
    private String customerId;
    private double maxAmount;
    private String maxAmountTxt;
    private String swift;
    private String currency;
    private int notYetDeducted;
    private String notYetDeductedTxt;

    public int getGrantedOverdraft() {
        return this.grantedOverdraft;
    }

    public String getGrantedOverdraftTxt() {
        return this.grantedOverdraftTxt;
    }

    public String getGrantedOverdraftDueDate() {
        return this.grantedOverdraftDueDate;
    }

    public boolean isNemKonto() {
        return this.nemKonto;
    }

    public boolean isStdAccount() {
        return this.stdAccount;
    }

    public String getIban() {
        return this.iban;
    }

    public String getAccountType() {
        return this.accountType;
    }

    public String getAccountName() {
        return this.accountName;
    }

    public String getAccountId() {
        return this.accountId;
    }

    public String getAccountHolder() {
        return this.accountHolder;
    }

    public String getCustomerId() {
        return this.customerId;
    }

    public double getMaxAmount() {
        return this.maxAmount;
    }

    public String getMaxAmountTxt() {
        return this.maxAmountTxt;
    }

    public String getSwift() {
        return this.swift;
    }

    public String getCurrency() {
        return this.currency;
    }

    public int getNotYetDeducted() {
        return this.notYetDeducted;
    }

    public String getNotYetDeductedTxt() {
        return this.notYetDeductedTxt;
    }

    public Amount getTinkMaxAmount() {
        return new Amount(this.currency, this.maxAmount);
    }

    public boolean isTransactionalAccount() {
        return hasType(TransactionalAccount.ALLOWED_ACCOUNT_TYPES::contains);
    }

    public boolean isCreditCardAccount() {
        return hasType(accountTypes -> AccountTypes.CREDIT_CARD == accountTypes);
    }

    private boolean hasType(Predicate<AccountTypes> predicate) {
        Optional<AccountTypes> accountType = getTinkAccountType();
        return accountType.filter(predicate).isPresent();
    }

    public boolean isUnknownType() {
        return !getTinkAccountType().isPresent();
    }

    public Optional<AccountTypes> getTinkAccountType() {
        for (Map.Entry<String, AccountTypes> entry : BecConstants.ACCOUNT_TYPES.entrySet()) {
            if (this.accountType.toLowerCase().contains(entry.getKey())) {
                return Optional.of(entry.getValue());
            }
        }
        return Optional.empty();
    }
}
