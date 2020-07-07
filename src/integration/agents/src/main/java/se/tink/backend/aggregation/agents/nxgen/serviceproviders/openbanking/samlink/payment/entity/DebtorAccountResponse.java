package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.payment.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
public class DebtorAccountResponse {

    private String iban;
    private String bban;
    private String pan;
    private String maskedPan;
    private String msisdn;
    private String currency;

    @JsonIgnore
    public Debtor toTinkDebtor() {
        return new Debtor(AccountIdentifier.create(Type.IBAN, iban));
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
