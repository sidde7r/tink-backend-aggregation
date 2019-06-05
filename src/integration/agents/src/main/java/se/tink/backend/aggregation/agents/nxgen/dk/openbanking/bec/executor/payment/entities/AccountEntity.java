package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
public class AccountEntity {

    private String bban;
    private String iban;

    public AccountEntity() {}

    public AccountEntity(String bban, String iban) {
        this.bban = bban;
        this.iban = iban;
    }

    public Creditor toTinkCreditor() {
        return new Creditor(AccountIdentifier.create(Type.IBAN, bban));
    }

    public Debtor toTinkDebtor() {
        return new Debtor(AccountIdentifier.create(Type.IBAN, bban));
    }
}
