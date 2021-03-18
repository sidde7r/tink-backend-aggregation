package se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.executor.payment.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.payment.rpc.Creditor;

@JsonObject
public class CreditorAccountRequest {

    private String iban;
    private String bban;
    private String pan;
    private String maskedPan;
    private String msisdn;
    private String currency;

    @JsonIgnore
    private CreditorAccountRequest(
            String iban,
            String bban,
            String pan,
            String maskedPan,
            String msisdn,
            String currency) {
        this.iban = iban;
        this.bban = bban;
        this.pan = pan;
        this.maskedPan = maskedPan;
        this.msisdn = msisdn;
        this.currency = currency;
    }

    public static CreditorAccountBuilder builder() {
        return new CreditorAccountBuilder();
    }

    @JsonIgnore
    public Creditor toTinkCreditor() {
        return new Creditor(AccountIdentifier.create(AccountIdentifierType.IBAN, iban));
    }

    public static class CreditorAccountBuilder {

        private String iban;
        private String bban;
        private String pan;
        private String maskedPan;
        private String msisdn;
        private String currency;

        CreditorAccountBuilder() {}

        public CreditorAccountBuilder iban(String iban) {
            this.iban = iban;
            return this;
        }

        public CreditorAccountBuilder bban(String bban) {
            this.bban = bban;
            return this;
        }

        public CreditorAccountBuilder pan(String pan) {
            this.pan = pan;
            return this;
        }

        public CreditorAccountBuilder maskedPan(String maskedPan) {
            this.maskedPan = maskedPan;
            return this;
        }

        public CreditorAccountBuilder msisdn(String msisdn) {
            this.msisdn = msisdn;
            return this;
        }

        public CreditorAccountBuilder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public CreditorAccountRequest build() {
            return new CreditorAccountRequest(iban, bban, pan, maskedPan, msisdn, currency);
        }
    }
}
