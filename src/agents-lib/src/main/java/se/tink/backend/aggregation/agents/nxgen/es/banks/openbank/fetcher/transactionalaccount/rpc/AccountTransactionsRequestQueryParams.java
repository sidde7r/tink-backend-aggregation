package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.transactionalaccount.rpc;

public class AccountTransactionsRequestQueryParams {
    private String productCode;
    private String contractNumber;
    private String fromDate;
    private String toDate;

    private AccountTransactionsRequestQueryParams(Builder builder) {
        productCode = builder.productCode;
        contractNumber = builder.contractNumber;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public static class Builder {
        private String productCode;
        private String contractNumber;
        private String fromDate;
        private String toDate;

        public Builder withProductCode(String productCode) {
            this.productCode = productCode;
            return this;
        }

        public Builder withContractNumber(String contractNumber) {
            this.contractNumber = contractNumber;
            return this;
        }

        public Builder withFromDate(String fromDate) {
            this.fromDate = fromDate;
            return this;
        }

        public Builder withToDate(String toDate) {
            this.toDate = toDate;
            return this;
        }

        public AccountTransactionsRequestQueryParams build() {
            return new AccountTransactionsRequestQueryParams(this);
        }
    }
}
