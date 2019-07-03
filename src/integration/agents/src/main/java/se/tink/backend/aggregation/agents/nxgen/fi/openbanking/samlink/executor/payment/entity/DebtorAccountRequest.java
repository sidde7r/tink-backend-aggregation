package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink.executor.payment.entity;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
public class DebtorAccountRequest {

    private String iban;

    public DebtorAccountRequest(String iban) {
        this.iban = iban;
    }

    public Debtor toTinkDebtor() {
        return new Debtor(AccountIdentifier.create(Type.IBAN, iban));
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }
}
