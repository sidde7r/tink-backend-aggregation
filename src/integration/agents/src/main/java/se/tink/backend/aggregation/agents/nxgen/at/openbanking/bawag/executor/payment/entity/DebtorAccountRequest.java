package se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.executor.payment.entity;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
public class DebtorAccountRequest extends Account {

    private DebtorAccountRequest(
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

    public static DebtorAccountBuilder builder() {
        return new DebtorAccountBuilder();
    }

    public Debtor toTinkDebtor() {
        return new Debtor(AccountIdentifier.create(AccountIdentifierType.IBAN, iban));
    }

    public static class DebtorAccountBuilder {

        private String iban;
        private String bban;
        private String pan;
        private String maskedPan;
        private String msisdn;
        private String currency;

        DebtorAccountBuilder() {}

        public DebtorAccountBuilder iban(String iban) {
            this.iban = iban;
            return this;
        }

        public DebtorAccountBuilder bban(String bban) {
            this.bban = bban;
            return this;
        }

        public DebtorAccountBuilder pan(String pan) {
            this.pan = pan;
            return this;
        }

        public DebtorAccountBuilder maskedPan(String maskedPan) {
            this.maskedPan = maskedPan;
            return this;
        }

        public DebtorAccountBuilder msisdn(String msisdn) {
            this.msisdn = msisdn;
            return this;
        }

        public DebtorAccountBuilder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public DebtorAccountRequest build() {
            return new DebtorAccountRequest(iban, bban, pan, maskedPan, msisdn, currency);
        }
    }
}
