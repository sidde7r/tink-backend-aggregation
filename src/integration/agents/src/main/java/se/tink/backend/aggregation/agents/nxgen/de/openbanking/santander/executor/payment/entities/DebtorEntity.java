package se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
public class DebtorEntity {

    private String iban;
    private String currency;

    public DebtorEntity(String iban, String currency) {
        this.iban = iban;
        this.currency = currency;
    }

    public DebtorEntity() {}

    public Debtor toTinkDebtor() {
        return new Debtor(AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban));
    }
}
