package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.payment.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.payment.rpc.Creditor;

@JsonObject
public class CreditorAccountRequest {

    private String iban;

    @JsonIgnore
    public CreditorAccountRequest(String iban) {
        this.iban = iban;
    }

    @JsonIgnore
    public Creditor toTinkCreditor() {
        return new Creditor(AccountIdentifier.create(Type.IBAN, iban));
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }
}
