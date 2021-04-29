package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsRequestBody {

    @JsonInclude(Include.NON_NULL)
    private String accountNumber;

    @JsonInclude(Include.NON_NULL)
    private String internalKey;

    @JsonInclude(Include.NON_NULL)
    private String dateFrom;

    private boolean needOtp;

    private TransactionsRequestBody(Builder builder) {
        this.needOtp = builder.needOtp;
        this.accountNumber = builder.accountNumber;
        this.dateFrom = builder.dateFrom;
        this.internalKey = builder.internalKey;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String accountNumber;
        private String internalKey;
        private String dateFrom;
        private boolean needOtp;

        public Builder() {
            this.needOtp = false;
        }

        public Builder withAccountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
            return this;
        }

        public Builder withDateFrom(String dateFrom) {
            this.dateFrom = dateFrom;
            return this;
        }

        public Builder withInternalKey(String internalKey) {
            this.internalKey = internalKey;
            return this;
        }

        public TransactionsRequestBody build() {
            return new TransactionsRequestBody(this);
        }
    }
}
