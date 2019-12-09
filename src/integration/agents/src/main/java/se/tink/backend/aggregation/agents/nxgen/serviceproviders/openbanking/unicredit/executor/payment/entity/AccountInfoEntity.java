package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.entity;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
public class AccountInfoEntity {

    private String iban;
    private String currency;

    public Creditor toTinkCreditor() {
        return new Creditor(AccountIdentifier.create(Type.IBAN, iban));
    }

    public Debtor toTinkDebtor() {
        return new Debtor(AccountIdentifier.create(Type.IBAN, iban));
    }
}
