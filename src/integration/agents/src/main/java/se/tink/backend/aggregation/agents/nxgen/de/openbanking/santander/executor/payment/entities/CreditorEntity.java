package se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.payment.rpc.Creditor;

@JsonObject
public class CreditorEntity {
    private String iban;
    private String currency;

    public CreditorEntity(String iban, String currency) {
        this.iban = iban;
        this.currency = currency;
    }

    public CreditorEntity() {}

    public Creditor toTinkCreditor() {
        return new Creditor(AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban));
    }
}
