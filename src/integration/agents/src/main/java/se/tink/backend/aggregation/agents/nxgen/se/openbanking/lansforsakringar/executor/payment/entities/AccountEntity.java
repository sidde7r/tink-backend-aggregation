package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
public class AccountEntity {

    private String bban;
    private String currency;

    public AccountEntity() {}

    public AccountEntity(String bban, String currency) {
        this.bban = bban;
        this.currency = currency;
    }

    public Creditor toTinkCreditor() {
        return new Creditor(AccountIdentifier.create(AccountIdentifier.Type.SE, bban));
    }

    public Debtor toTinkDebtor() {
        return new Debtor(AccountIdentifier.create(AccountIdentifier.Type.SE, bban));
    }
}
