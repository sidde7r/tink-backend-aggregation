package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.enums.DnbPaymentType;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class CreatePaymentRequest {

    @JsonProperty("creditorAccount")
    private AccountEntity creditor;

    @JsonProperty("debtorAccount")
    private AccountEntity debtor;

    @JsonProperty("instructedAmount")
    private AmountEntity amount;

    private String creditorName;

    private String creditorAgent;

    private String regulatoryReportingInformation;

    private String regulatoryReportingCode;

    private CreatePaymentRequest(Builder builder) {
        this.creditor = builder.creditor;
        this.creditorName = builder.creditorName;
        this.creditorAgent = builder.creditorAgent;
        this.debtor = builder.debtor;
        this.amount = builder.amount;
        this.regulatoryReportingInformation = builder.regulatoryReportingInformation;
        this.regulatoryReportingCode = builder.regulatoryReportingCode;
    }

    public static class Builder {
        private DnbPaymentType dnbPaymentType;
        private AccountEntity creditor;
        private String creditorName;
        private String creditorAgent;
        private AccountEntity debtor;
        private AmountEntity amount;
        private String regulatoryReportingInformation;
        private String regulatoryReportingCode;

        public Builder withPaymentType(DnbPaymentType dnbPaymentType) {
            this.dnbPaymentType = dnbPaymentType;
            return this;
        }

        public Builder withCreditor(AccountEntity creditor) {
            this.creditor = creditor;
            return this;
        }

        public Builder withCreditorName(String creditorName) {
            this.creditorName = creditorName;
            return this;
        }

        public Builder withDebtor(AccountEntity debtor) {
            this.debtor = debtor;
            return this;
        }

        public Builder withAmount(AmountEntity amount) {
            this.amount = amount;
            return this;
        }

        public Builder withAdditionalInformation(
                String creditorAgent,
                String regulatoryReportingCode,
                String regulatoryReportingInformation) {
            if (dnbPaymentType.equals(DnbPaymentType.NorwegianCrossBorderCreditTransfers)) {
                this.creditorAgent = creditorAgent;
                this.regulatoryReportingCode = regulatoryReportingCode;
                this.regulatoryReportingInformation = regulatoryReportingInformation;
            }

            return this;
        }

        public CreatePaymentRequest build() {
            return new CreatePaymentRequest(this);
        }
    }
}
