package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor
public class AccountEntity {

    private String iban;

    public static AccountEntity creditorOf(PaymentRequest paymentRequest) {
        return new AccountEntity(paymentRequest.getPayment().getCreditor().getAccountNumber());
    }

    public static AccountEntity debtorOf(PaymentRequest paymentRequest) {
        return Optional.ofNullable(paymentRequest.getPayment().getDebtor())
                .map(debtor -> new AccountEntity(debtor.getAccountNumber()))
                .orElse(new AccountEntity());
    }

    public Creditor toTinkCreditor() {
        return new Creditor(new IbanIdentifier(iban));
    }

    public Debtor toTinkDebtor() {
        if (iban == null) {
            return null;
        }

        return new Debtor(new IbanIdentifier(iban));
    }
}
