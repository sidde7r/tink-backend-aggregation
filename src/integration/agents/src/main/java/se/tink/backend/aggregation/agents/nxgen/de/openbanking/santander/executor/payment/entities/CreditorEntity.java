package se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.payment.rpc.Creditor;

@JsonObject
public class CreditorEntity {
    private String iban;
    private String currency;

    @JsonIgnore
    public CreditorEntity(String iban, String currency) {
        this.iban = iban;
        this.currency = currency;
    }

    public CreditorEntity() {}

    @JsonIgnore
    public Creditor toTinkCreditor() {
        return new Creditor(AccountIdentifier.create(AccountIdentifierType.IBAN, iban));
    }
}
