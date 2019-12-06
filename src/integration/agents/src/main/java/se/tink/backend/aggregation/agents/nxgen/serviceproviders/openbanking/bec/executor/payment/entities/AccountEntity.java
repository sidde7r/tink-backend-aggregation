package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
public class AccountEntity {

    private String bban;
    private String iban;

    public AccountEntity() {}

    @JsonIgnore
    public AccountEntity(Builder builder) {
        this.bban = builder.bban;
        this.iban = builder.iban;
    }

    @JsonIgnore
    public Creditor toTinkCreditor() {
        return new Creditor(AccountIdentifier.create(Type.DK, bban));
    }

    @JsonIgnore
    public Debtor toTinkDebtor() {
        return new Debtor(AccountIdentifier.create(Type.DK, bban));
    }

    public static class Builder {
        private String iban;
        private String bban;

        public Builder setIban(String iban) {
            this.iban = iban;
            return this;
        }

        public Builder setBban(String bban) {
            this.bban = bban;
            return this;
        }

        public AccountEntity build() {
            return new AccountEntity(this);
        }
    }
}
