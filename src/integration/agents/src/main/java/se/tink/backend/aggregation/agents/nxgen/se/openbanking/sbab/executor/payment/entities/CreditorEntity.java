package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.payment.rpc.Creditor;

@JsonObject
public class CreditorEntity {
    private AccountEntity account;
    private String name;

    public CreditorEntity() {}

    @JsonIgnore
    private CreditorEntity(Builder builder) {
        this.account = builder.account;
        this.name = builder.name;
    }

    @JsonIgnore
    public static CreditorEntity of(PaymentRequest paymentRequest) {
        return new CreditorEntity.Builder().withAccount(new AccountEntity()).build();
    }

    @JsonIgnore
    public Creditor toTinkCreditor() {
        return new Creditor(AccountIdentifier.create(Type.SE, account.getIdentifier()));
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

        public CreditorEntity build() {
            return new CreditorEntity(this);
        }
    }
}
