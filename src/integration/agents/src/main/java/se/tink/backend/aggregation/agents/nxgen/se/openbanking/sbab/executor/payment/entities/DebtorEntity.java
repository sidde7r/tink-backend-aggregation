package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
public class DebtorEntity {
    private AccountEntity account;
    private String name;

    public DebtorEntity() {}

    @JsonIgnore
    private DebtorEntity(Builder builder) {
        this.account = builder.account;
        this.name = builder.name;
    }

    @JsonIgnore
    public static DebtorEntity of(PaymentRequest paymentRequest) {
        return new DebtorEntity.Builder().withAccount(new AccountEntity()).build();
    }

    @JsonIgnore
    public Debtor toTinkDebtor() {
        return new Debtor((AccountIdentifier.create(Type.SE, account.getIdentifier())));
    }

    public String getAccountNumber() {
        return account.getIdentifier();
    }

    public String getName() {
        return name;
    }

    public static class Builder {
        private AccountEntity account;
        private String name;

        public Builder withAccount(AccountEntity account) {
            this.account = account;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public DebtorEntity build() {
            return new DebtorEntity(this);
        }
    }
}
