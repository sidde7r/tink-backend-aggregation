package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
public class AccountEntity {

    private String iban;

    public static AccountEntity creditorOf(PaymentRequest paymentRequest) {
        IbanIdentifier accountIdentifier =
                (IbanIdentifier) paymentRequest.getPayment().getCreditor().getAccountIdentifier();
        return new AccountEntity(accountIdentifier.getIban());
    }

    public static AccountEntity debtorOf(PaymentRequest paymentRequest) {
        return Optional.ofNullable(paymentRequest.getPayment().getDebtor())
                .map(
                        debtor ->
                                new AccountEntity(
                                        ((IbanIdentifier) debtor.getAccountIdentifier()).getIban()))
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

    @JsonIgnore
    public boolean isFrenchIban() {
        return iban != null && iban.startsWith("FR");
    }

    @JsonIgnore
    public boolean isMonacoIban() {
        return iban != null && iban.startsWith("MC");
    }
}
