package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transfer.entities;

import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

@JsonObject
public class PaymentAccountsEntity implements GeneralAccountEntity {
    private String accountNumber;
    private double balance;
    private String accountName;
    private String clearingNumber;
    private boolean youthAccount;
    private String productCode;
    private boolean investmentAccount;
    private boolean localAccount;
    private String displayText;
    private String accountType;

    public String getAccountNumber() {
        return accountNumber;
    }

    public double getBalance() {
        return balance;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getClearingNumber() {
        return clearingNumber;
    }

    public boolean isYouthAccount() {
        return youthAccount;
    }

    public String getProductCode() {
        return productCode;
    }

    public boolean isInvestmentAccount() {
        return investmentAccount;
    }

    public boolean isLocalAccount() {
        return localAccount;
    }

    public String getDisplayText() {
        return displayText;
    }

    public String getAccountType() {
        return accountType;
    }

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        return new SwedishIdentifier(accountNumber);
    }

    @Override
    public String generalGetBank() {
        return null;
    }

    @Override
    public String generalGetName() {
        return accountName;
    }
}
