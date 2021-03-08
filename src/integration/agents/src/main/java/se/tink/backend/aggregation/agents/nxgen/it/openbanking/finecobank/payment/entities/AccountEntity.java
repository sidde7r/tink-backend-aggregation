package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AccountEntity {
    private String iban;

    @JsonIgnore
    public Creditor toTinkCreditor() {
        return new Creditor(new IbanIdentifier(iban));
    }

    @JsonIgnore
    public Debtor toTinkDebtor() {
        return new Debtor(new IbanIdentifier(iban));
    }
}
