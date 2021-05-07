package se.tink.backend.aggregation.agents.utils.berlingroup.payment.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AccountEntity {
    private String iban;

    public Creditor toTinkCreditor() {
        return new Creditor(new IbanIdentifier(iban));
    }

    public Debtor toTinkDebtor() {
        return new Debtor(new IbanIdentifier(iban));
    }
}
