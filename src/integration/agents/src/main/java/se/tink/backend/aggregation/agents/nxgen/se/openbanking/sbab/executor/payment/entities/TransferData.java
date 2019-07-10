package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransferData {

    @JsonProperty("counter_part_account")
    private String counterPartAccount;

    @JsonProperty("amount_to_transfer")
    private Number amountToTransfer;

    private String statement;

    private String currency;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("transfer_date")
    private String transferDate;

    @JsonProperty("counter_part_statement")
    private String counterPartStatement;

    private TransferData(Builder builder) {
        this.amountToTransfer = builder.amountToTransfer;
        this.counterPartAccount = builder.counterPartAccount;
        this.currency = builder.currency;
        this.statement = builder.statement;
        this.transferDate = builder.transferDate;
    }

    @JsonIgnore
    public String getCounterPartAccount() {
        return counterPartAccount;
    }

    public static class Builder {
        private Number amountToTransfer;
        private String counterPartAccount;
        private String statement;
        private String currency;
        private String counterPartStatement;

        @JsonFormat(pattern = "yyyy-MM-dd")
        @JsonProperty("transfer_date")
        private String transferDate;

        public Builder withAmount(Number amountToTransfer) {
            this.amountToTransfer = amountToTransfer;
            return this;
        }

        public Builder withCounterPartAccount(String counterPartAccount) {
            this.counterPartAccount = counterPartAccount;
            return this;
        }

        public Builder withCurrency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder withStatement(String statement) {
            this.statement = statement;
            return this;
        }

        public Builder withCounterPartStatement(String counterPartStatement) {
            this.counterPartStatement = counterPartStatement;
            return this;
        }

        public Builder withTransferDate(String transferDate) {
            this.transferDate = transferDate;
            return this;
        }

        public TransferData build() {
            return new TransferData(this);
        }
    }
}
