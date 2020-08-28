package se.tink.backend.aggregation.agents.banks.se.collector.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import se.tink.libraries.account.AccountIdentifier;

@JsonIgnoreProperties(ignoreUnknown = true)
class WithdrawalAccount {
    @JsonIgnore private String bank;

    @JsonProperty("BankPrefix")
    private String clearingNumber;

    @JsonProperty("AccountNr")
    private String accountNumber;

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getClearingNumber() {
        return clearingNumber;
    }

    public void setClearingNumber(String clearingNumber) {
        this.clearingNumber = clearingNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    AccountIdentifier toIdentifier() {
        return AccountIdentifier.create(AccountIdentifier.Type.SE, clearingNumber + accountNumber);
    }

    private String mask(String s) {
        if (Strings.isNullOrEmpty(s)) {
            return null;
        }
        // mask half of the input string, do not disclose how long the original string is
        return "*****" + s.substring(s.length() / 2);
    }

    @Override
    public String toString() {
        MoreObjects.ToStringHelper builder =
                MoreObjects.toStringHelper(this)
                        .add("BankClearingNr", mask(clearingNumber))
                        .add("BankAccountNr", mask(accountNumber));

        if (Strings.isNullOrEmpty(bank)) {
            builder.add("Bank", bank);
        }

        return builder.toString();
    }
}
