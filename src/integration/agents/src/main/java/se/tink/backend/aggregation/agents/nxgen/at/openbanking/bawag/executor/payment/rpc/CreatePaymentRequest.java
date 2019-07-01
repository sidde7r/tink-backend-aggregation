package se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.executor.payment.rpc;

import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.executor.payment.entity.CreditorAccountRequest;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.executor.payment.entity.CreditorAddress;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.executor.payment.entity.DebtorAccountRequest;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.executor.payment.entity.InstructedAmountRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreatePaymentRequest {
    private final CreditorAccountRequest creditorAccount;
    private final CreditorAddress creditorAddress;
    private final String creditorAgent;
    private final String creditorName;
    private final DebtorAccountRequest debtorAccount;
    private final String endToEndIdentification;
    private final InstructedAmountRequest instructedAmount;
    private final String remittanceInformationUnstructured;
    private final String requestedExecutionDate;

    private CreatePaymentRequest(
            CreditorAccountRequest creditorAccount,
            CreditorAddress creditorAddress,
            String creditorAgent,
            String creditorName,
            DebtorAccountRequest debtorAccount,
            String endToEndIdentification,
            InstructedAmountRequest instructedAmount,
            String remittanceInformationUnstructured,
            String requestedExecutionDate) {
        this.creditorAccount = creditorAccount;
        this.creditorAddress = creditorAddress;
        this.creditorAgent = creditorAgent;
        this.creditorName = creditorName;
        this.debtorAccount = debtorAccount;
        this.endToEndIdentification = endToEndIdentification;
        this.instructedAmount = instructedAmount;
        this.remittanceInformationUnstructured = remittanceInformationUnstructured;
        this.requestedExecutionDate = requestedExecutionDate;
    }

    public static CreatePaymentRequestBuilder builder() {
        return new CreatePaymentRequestBuilder();
    }

    public static class CreatePaymentRequestBuilder {

        private CreditorAccountRequest creditorAccount;
        private CreditorAddress creditorAddress;
        private String creditorAgent;
        private String creditorName;
        private DebtorAccountRequest debtorAccount;
        private String endToEndIdentification;
        private InstructedAmountRequest instructedAmount;
        private String remittanceInformationUnstructured;
        private String requestedExecutionDate;

        CreatePaymentRequestBuilder() {}

        public CreatePaymentRequestBuilder creditorAccount(CreditorAccountRequest creditorAccount) {
            this.creditorAccount = creditorAccount;
            return this;
        }

        public CreatePaymentRequestBuilder creditorAddress(CreditorAddress creditorAddress) {
            this.creditorAddress = creditorAddress;
            return this;
        }

        public CreatePaymentRequestBuilder creditorAgent(String creditorAgent) {
            this.creditorAgent = creditorAgent;
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

        public CreatePaymentRequestBuilder endToEndIdentification(String endToEndIdentification) {
            this.endToEndIdentification = endToEndIdentification;
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

        public CreatePaymentRequestBuilder requestedExecutionDate(String requestedExecutionDate) {
            this.requestedExecutionDate = requestedExecutionDate;
            return this;
        }

        public CreatePaymentRequest build() {
            return new CreatePaymentRequest(
                    creditorAccount,
                    creditorAddress,
                    creditorAgent,
                    creditorName,
                    debtorAccount,
                    endToEndIdentification,
                    instructedAmount,
                    remittanceInformationUnstructured,
                    requestedExecutionDate);
        }

        public String toString() {
            return "CreatePaymentRequest.CreatePaymentRequestBuilder(creditorAccount="
                    + this.creditorAccount
                    + ", creditorAddress="
                    + this.creditorAddress
                    + ", creditorAgent="
                    + this.creditorAgent
                    + ", creditorName="
                    + this.creditorName
                    + ", debtorAccount="
                    + this.debtorAccount
                    + ", endToEndIdentification="
                    + this.endToEndIdentification
                    + ", instructedAmount="
                    + this.instructedAmount
                    + ", remittanceInformationUnstructured="
                    + this.remittanceInformationUnstructured
                    + ", requestedExecutionDate="
                    + this.requestedExecutionDate
                    + ")";
        }
    }
}
