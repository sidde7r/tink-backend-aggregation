package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.payment.rpc.Creditor;

@JsonObject
public class AccountIbanEntity {
    private String currency;
    private String iban;

    public AccountIbanEntity() {}

    public AccountIbanEntity(String iban, String currency) {
        this.iban = iban;
        this.currency = currency;
    }

    public Creditor toTinkCreditor() {
        return new Creditor(AccountIdentifier.create(AccountIdentifierType.IBAN, iban));
    }
}
