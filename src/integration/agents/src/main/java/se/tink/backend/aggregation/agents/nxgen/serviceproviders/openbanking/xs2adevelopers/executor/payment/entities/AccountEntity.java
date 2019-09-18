package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class AccountEntity {
    private String bban;
    private String currency;
    private String iban;
    private String maskedPan;
    private String msisdn;

    public AccountEntity() {}

    public AccountEntity(String iban) {
        this.iban = iban;
    }

    public Creditor toTinkCreditor() {
        return new Creditor(new IbanIdentifier(iban));
    }

    public Debtor toTinkDebtor() {
        return new Debtor(new IbanIdentifier(iban));
    }
}
