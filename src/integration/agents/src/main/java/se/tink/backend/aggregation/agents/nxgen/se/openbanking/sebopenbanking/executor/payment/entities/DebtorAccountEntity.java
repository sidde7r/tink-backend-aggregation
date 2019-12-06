package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
public class DebtorAccountEntity {
    private String iban;

    public DebtorAccountEntity() {}

    public DebtorAccountEntity(String iban) {
        this.iban = iban;
    }

    @JsonIgnore
    public Debtor toTinkDebtor() {
        return new Debtor(new IbanIdentifier(iban));
    }

    @JsonIgnore
    public static DebtorAccountEntity of(PaymentRequest paymentRequest) {
        return new DebtorAccountEntity(paymentRequest.getPayment().getDebtor().getAccountNumber());
    }
}
