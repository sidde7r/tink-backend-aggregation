package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
public class AccountEntity {

    @JsonCreator
    public AccountEntity(@JsonProperty("iban") String iban) {
        this.iban = iban;
    }

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
