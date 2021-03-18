package se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.executor.payment.entity;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.payment.rpc.Creditor;

@JsonObject
public class CreditorAccountResponse extends Account {

    public CreditorAccountResponse() {}

    public String getIban() {
        return iban;
    }

    public String getBban() {
        return bban;
    }

    public String getPan() {
        return pan;
    }

    public String getMaskedPan() {
        return maskedPan;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public String getCurrency() {
        return currency;
    }

    public Creditor toTinkCreditor() {
        return new Creditor(AccountIdentifier.create(AccountIdentifierType.IBAN, iban));
    }
}
