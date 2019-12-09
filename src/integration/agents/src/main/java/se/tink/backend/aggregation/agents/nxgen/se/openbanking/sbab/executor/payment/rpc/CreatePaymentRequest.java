package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.entities.TransferData;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreatePaymentRequest {

    @JsonProperty("transfer_data")
    private TransferData transferData;

    public CreatePaymentRequest(Builder builder) {
        this.transferData = builder.transferData;
    }

    public static class Builder {
        private TransferData transferData;

        public Builder withTransferData(TransferData transferData) {
            this.transferData = transferData;
            return this;
        }

        public CreatePaymentRequest build() {
            return new CreatePaymentRequest(this);
        }
    }
}
