package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditorEntity {
    private AccountEntity account;
    private String message;
    private String name;
    private ReferenceEntity reference;

    public CreditorEntity() {}

    @JsonIgnore
    private CreditorEntity(Builder builder) {
        this.account = builder.account;
        this.message = builder.message;
        this.name = builder.name;
        this.reference = builder.reference;
    }

    public static class Builder {
        private AccountEntity account;
        private String message;
        private String name;
        private ReferenceEntity reference;

        public Builder withAccount(AccountEntity account) {
            this.account = account;
            return this;
        }

        public Builder withMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withReference(ReferenceEntity reference) {
            this.reference = reference;
            return this;
        }

        public CreditorEntity build() {
            return new CreditorEntity(this);
        }
    }
}
