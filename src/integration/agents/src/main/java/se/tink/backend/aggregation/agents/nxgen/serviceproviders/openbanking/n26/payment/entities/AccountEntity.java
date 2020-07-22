package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.payment.entities;

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

    private String bic;

    public static AccountEntity creditorOf(PaymentRequest paymentRequest) {
        IbanIdentifier accountIdentifier =
                (IbanIdentifier) paymentRequest.getPayment().getCreditor().getAccountIdentifier();
        return new AccountEntity(accountIdentifier.getIban(), accountIdentifier.getBic());
    }

    public static AccountEntity debtorOf(PaymentRequest paymentRequest) {
        return Optional.ofNullable(paymentRequest.getPayment().getDebtor())
                .map(
                        debtor ->
                                new AccountEntity(
                                        ((IbanIdentifier) debtor.getAccountIdentifier()).getIban(),
                                        ((IbanIdentifier) debtor.getAccountIdentifier()).getBic()))
                .orElse(new AccountEntity());
    }

    public Creditor toTinkCreditor() {
        return new Creditor(new IbanIdentifier(iban, bic));
    }

    public Debtor toTinkDebtor() {
        if (iban == null) {
            return null;
        }

        return new Debtor(new IbanIdentifier(iban, bic));
    }
}
