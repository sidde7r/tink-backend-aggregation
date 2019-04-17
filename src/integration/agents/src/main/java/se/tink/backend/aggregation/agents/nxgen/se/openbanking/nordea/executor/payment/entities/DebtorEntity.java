package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.enums.NordeaAccountType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.payment.rpc.Debtor;

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

    @JsonIgnore
    public static DebtorEntity of(PaymentRequest paymentRequest) {
        return new DebtorEntity.Builder()
                .withAccount(
                        new AccountEntity(
                                NordeaAccountType.mapToNordeaAccountType(
                                                paymentRequest
                                                        .getPayment()
                                                        .getDebtor()
                                                        .getAccountIdentifierType())
                                        .name(),
                                paymentRequest.getPayment().getCurrency(),
                                paymentRequest.getPayment().getDebtor().getAccountNumber()))
                .build();
    }

    @JsonIgnore
    public Debtor toTinkDebtor() {
        return new Debtor(account.toTinkAccountIdentifier());
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
