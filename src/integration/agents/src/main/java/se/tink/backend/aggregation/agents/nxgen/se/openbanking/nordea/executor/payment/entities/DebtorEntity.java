package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DebtorEntity {
    private AccountEntity account;
    private String message;

    public DebtorEntity() {}

    @JsonIgnore
    private DebtorEntity(Builder builder) {
        this.account = builder.account;
        this.message = builder.message;
    }

    public static class Builder {
        private AccountEntity account;
        private String message;

        public DebtorEntity.Builder withAccount(AccountEntity account) {
            this.account = account;
            return this;
        }

        public DebtorEntity.Builder withMessage(String message) {
            this.message = message;
            return this;
        }

        public DebtorEntity build() {
            return new DebtorEntity(this);
        }
    }
}
