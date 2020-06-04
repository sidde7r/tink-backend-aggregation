package se.tink.libraries.payment.rpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Beneficiary {
    private final String name;
    private final String accountNumber;
    private final AccountIdentifier.Type accountNumberType;

    public String getName() {
        return name;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public Type getAccountNumberType() {
        return accountNumberType;
    }

    @JsonCreator
    public Beneficiary(
            @JsonProperty("name") String name,
            @JsonProperty("accountNumber") String accountNumber,
            @JsonProperty("accountNumberType") AccountIdentifier.Type accountNumberType) {
        this.name = name;
        this.accountNumber = accountNumber;
        this.accountNumberType = accountNumberType;
    }

    @JsonIgnore
    private Beneficiary(Builder builder) {
        this.name = builder.name;
        this.accountNumber = builder.accountNumber;
        this.accountNumberType = builder.accountNumberType;
    }

    public static class Builder {
        private String name;
        private String accountNumber;
        private AccountIdentifier.Type accountNumberType;

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withAccountNumber(String accountNumber) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(accountNumber));
            this.accountNumber = accountNumber;
            return this;
        }

        public Builder withAccountNumberType(AccountIdentifier.Type accountNumberType) {
            Preconditions.checkNotNull(accountNumberType);
            this.accountNumberType = accountNumberType;
            return this;
        }

        public Beneficiary build() {
            return new Beneficiary(this);
        }
    }
}
