package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transfer.entities;

import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

@JsonObject
public class TransferAccountsEntity implements GeneralAccountEntity {
    private String accountNumber;
    private double balance;
    private String accountName;
    private String clearingNumber;
    private boolean localAccount;
    private String bankName;
    private String type;
    private boolean savedRecipient;
    // `accountInfoText` is null - cannot define it!

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

    public boolean isLocalAccount() {
        return localAccount;
    }

    public String getBankName() {
        return bankName;
    }

    public String getType() {
        return type;
    }

    public boolean isSavedRecipient() {
        return savedRecipient;
    }

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        return new SwedishIdentifier(accountNumber);
    }

    @Override
    public String generalGetBank() {
        return bankName;
    }

    @Override
    public String generalGetName() {
        return accountName;
    }
}
