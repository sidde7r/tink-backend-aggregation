package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.time.LocalDate;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.AccountReferenceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.executor.payment.entities.AddressEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.serializers.LocalDateSerializer;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonInclude(Include.NON_NULL)
public class CreatePaymentRequest {
    @JsonProperty private AmountEntity instructedAmount;

    @JsonProperty private AccountReferenceEntity debtorAccount;

    @JsonProperty private String creditorName;

    @JsonProperty private AccountReferenceEntity creditorAccount;

    @JsonProperty private String creditorAgent;

    @JsonProperty private AddressEntity creditorAddress;

    @JsonProperty private String remittanceInformationUnstructured;

    @JsonProperty private String chargeBearer;

    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate requestedExecutionDate;

    @JsonIgnore
    private CreatePaymentRequest(Builder builder) {
        this.instructedAmount = AmountEntity.withAmount(builder.amount);
        this.debtorAccount = builder.debtorAccount;
        this.creditorName = builder.creditorName;
        this.creditorAccount = builder.creditorAccount;
        this.creditorAgent = builder.creditorAgent;
        this.creditorAddress = builder.creditorAddress;
        this.remittanceInformationUnstructured = builder.remittanceInformation;
        this.chargeBearer = builder.chargeBearer;
        this.requestedExecutionDate = builder.requestedExecutionDate;
    }

    public static class Builder {
        private ExactCurrencyAmount amount;
        private AccountReferenceEntity debtorAccount;
        private String creditorName;
        private AccountReferenceEntity creditorAccount;
        private String creditorAgent;
        private AddressEntity creditorAddress;
        private String remittanceInformation;
        private String chargeBearer;
        private LocalDate requestedExecutionDate;

        public Builder withAmount(ExactCurrencyAmount amount) {
            this.amount = amount;
            return this;
        }

        public Builder withDebtorAccount(AccountReferenceEntity debtorAccount) {
            this.debtorAccount = debtorAccount;
            return this;
        }

        public Builder withCreditorName(String creditorName) {
            this.creditorName = creditorName;
            return this;
        }

        public Builder withCreditorAccount(AccountReferenceEntity creditorAccount) {
            this.creditorAccount = creditorAccount;
            return this;
        }

        public Builder withCreditorAgent(String creditorAgent) {
            this.creditorAgent = creditorAgent;
            return this;
        }

        public Builder withCreditorAddress(AddressEntity creditorAddress) {
            this.creditorAddress = creditorAddress;
            return this;
        }

        public Builder withRemittanceInformation(String remittanceInformation) {
            this.remittanceInformation = remittanceInformation;
            return this;
        }

        public Builder withChargeBearer(String chargeBearer) {
            this.chargeBearer = chargeBearer;
            return this;
        }

        public Builder withRequestedExecutionDate(LocalDate requestedExecutionDate) {
            this.requestedExecutionDate = requestedExecutionDate;
            return this;
        }

        public CreatePaymentRequest build() {
            Preconditions.checkNotNull(
                    Strings.emptyToNull(creditorName), "Creditor name must not be null.");
            return new CreatePaymentRequest(this);
        }
    }
}
