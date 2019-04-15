package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.enums.NordeaAccountType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.payment.rpc.Creditor;

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

    @JsonIgnore
    public static CreditorEntity of(Creditor internalCreditor) {
        return new CreditorEntity.Builder()
                .withAccount(
                        new AccountEntity(
                                NordeaAccountType.mapToNordeaAccountType(
                                                internalCreditor.getAccountIdentifierType())
                                        .name(),
                                internalCreditor.getCurrency(),
                                internalCreditor.getAccountNumber()))
                .build();
    }

    @JsonIgnore
    public Creditor toTinkCreditor() {
        return new Creditor(account.toTinkAccountIdentifier(), account.getCurrency());
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
