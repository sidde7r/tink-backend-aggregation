package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.entities.CreditorEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.entities.DebtorEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.entities.SignOptionsRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.entities.TransferData;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreatePaymentRequest {

    @JsonProperty("transfer_data")
    private TransferData transferData;

    @JsonProperty("sign_options_data")
    private SignOptionsRequest signOptionsData;

    private CreditorEntity creditor;
    private DebtorEntity debtor;

    public CreatePaymentRequest(Builder builder) {
        this.transferData = builder.transferData;
        this.signOptionsData = builder.signOptionsData;
        this.creditor = builder.creditor;
        this.debtor = builder.debtor;
    }

    @JsonIgnore
    public TransferData getTransferData() {
        return transferData;
    }

    public static class Builder {
        private SignOptionsRequest signOptionsData;
        private TransferData transferData;
        private CreditorEntity creditor;
        private DebtorEntity debtor;

        public Builder withTransferData(TransferData transferData) {
            this.transferData = transferData;
            return this;
        }

        public Builder withSignOptionsData(SignOptionsRequest signOptionsData) {
            this.signOptionsData = signOptionsData;
            return this;
        }

        public Builder withCreditor(CreditorEntity creditor) {
            this.creditor = creditor;
            return this;
        }

        public Builder withDebtor(DebtorEntity debtor) {
            this.debtor = debtor;
            return this;
        }

        public CreatePaymentRequest build() {
            return new CreatePaymentRequest(this);
        }
    }
}
