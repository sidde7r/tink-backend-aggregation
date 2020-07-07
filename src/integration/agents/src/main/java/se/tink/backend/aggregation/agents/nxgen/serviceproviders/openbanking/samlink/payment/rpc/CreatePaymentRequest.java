package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.payment.entity.CreditorAccountRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.payment.entity.CreditorAddress;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.payment.entity.DebtorAccountRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.payment.entity.InstructedAmountRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreatePaymentRequest {
    private final CreditorAccountRequest creditorAccount;
    private final CreditorAddress creditorAddress;
    private final String creditorName;
    private final DebtorAccountRequest debtorAccount;
    private final InstructedAmountRequest instructedAmount;
    private final String remittanceInformationUnstructured;

    @JsonIgnore private final boolean isSepa;

    @JsonIgnore
    private CreatePaymentRequest(
            CreditorAccountRequest creditorAccount,
            CreditorAddress creditorAddress,
            String creditorName,
            DebtorAccountRequest debtorAccount,
            InstructedAmountRequest instructedAmount,
            String remittanceInformationUnstructured,
            boolean isSepa) {
        this.creditorAccount = creditorAccount;
        this.creditorAddress = creditorAddress;
        this.creditorName = creditorName;
        this.debtorAccount = debtorAccount;
        this.instructedAmount = instructedAmount;
        this.remittanceInformationUnstructured = remittanceInformationUnstructured;
        this.isSepa = isSepa;
    }

    public static CreatePaymentRequestBuilder builder() {
        return new CreatePaymentRequestBuilder();
    }

    @JsonIgnore
    public boolean isSepa() {
        return isSepa;
    }

    public static class CreatePaymentRequestBuilder {

        private CreditorAccountRequest creditorAccount;
        private String creditorName;
        private DebtorAccountRequest debtorAccount;
        private InstructedAmountRequest instructedAmount;
        private CreditorAddress creditorAddress;
        private String remittanceInformationUnstructured;
        private boolean isSepa;

        CreatePaymentRequestBuilder() {}

        public CreatePaymentRequestBuilder creditorAccount(CreditorAccountRequest creditorAccount) {
            this.creditorAccount = creditorAccount;
            return this;
        }

        public CreatePaymentRequestBuilder creditorName(String creditorName) {
            this.creditorName = creditorName;
            return this;
        }

        public CreatePaymentRequestBuilder debtorAccount(DebtorAccountRequest debtorAccount) {
            this.debtorAccount = debtorAccount;
            return this;
        }

        public CreatePaymentRequestBuilder creditorAddress(CreditorAddress creditorAddress) {
            this.creditorAddress = creditorAddress;
            return this;
        }

        public CreatePaymentRequestBuilder instructedAmount(
                InstructedAmountRequest instructedAmount) {
            this.instructedAmount = instructedAmount;
            return this;
        }

        public CreatePaymentRequestBuilder remittanceInformationUnstructured(
                String remittanceInformationUnstructured) {
            this.remittanceInformationUnstructured = remittanceInformationUnstructured;
            return this;
        }

        public CreatePaymentRequestBuilder isSepa(boolean isSepa) {
            this.isSepa = isSepa;
            return this;
        }

        public CreatePaymentRequest build() {
            return new CreatePaymentRequest(
                    creditorAccount,
                    creditorAddress,
                    creditorName,
                    debtorAccount,
                    instructedAmount,
                    remittanceInformationUnstructured,
                    isSepa);
        }
    }
}
