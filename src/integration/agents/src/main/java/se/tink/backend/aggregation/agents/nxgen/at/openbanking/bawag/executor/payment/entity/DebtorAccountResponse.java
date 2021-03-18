package se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.executor.payment.entity;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
public class DebtorAccountResponse extends Account {

    public DebtorAccountResponse() {}

    public Debtor toTinkDebtor() {
        return new Debtor(AccountIdentifier.create(AccountIdentifierType.IBAN, iban));
    }

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
}
