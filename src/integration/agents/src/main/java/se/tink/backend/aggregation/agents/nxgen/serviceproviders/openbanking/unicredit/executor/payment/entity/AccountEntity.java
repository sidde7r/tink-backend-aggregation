package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.entity;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
public class AccountEntity {

    private String iban;

    public AccountEntity(String iban) {
        this.iban = iban;
    }

    public AccountEntity() {}

    public Debtor toTinkDebtor() {
        return new Debtor(AccountIdentifier.create(AccountIdentifierType.IBAN, iban));
    }
}
