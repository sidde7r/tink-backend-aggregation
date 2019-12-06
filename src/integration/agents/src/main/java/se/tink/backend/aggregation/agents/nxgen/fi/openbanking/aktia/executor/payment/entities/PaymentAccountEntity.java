package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
public class PaymentAccountEntity {

    private String iban;

    public PaymentAccountEntity() {}

    public PaymentAccountEntity(String iban) {
        this.iban = iban;
    }

    public Creditor toTinkCreditor() {
        return new Creditor(AccountIdentifier.create(Type.IBAN, iban));
    }

    public Debtor toTinkDebtor() {
        return new Debtor(AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban));
    }
}
