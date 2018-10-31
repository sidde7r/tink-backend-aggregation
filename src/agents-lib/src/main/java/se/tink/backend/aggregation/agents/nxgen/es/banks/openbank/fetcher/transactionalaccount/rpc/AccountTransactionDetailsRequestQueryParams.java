package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.transactionalaccount.rpc;

public class AccountTransactionDetailsRequestQueryParams {
    private String productCodeOld;
    private String contractNumberOld;
    private String productCodeNew;
    private String contractNumberNew;
    private String movementOfTheDayIndex;
    private String dateNoted;

    private AccountTransactionDetailsRequestQueryParams(Builder builder) {
        productCodeOld = builder.productCodeOld;
        contractNumberOld = builder.contractNumberOld;
        productCodeNew = builder.productCodeNew;
        contractNumberNew = builder.contractNumberNew;
        movementOfTheDayIndex = builder.movementOfTheDayIndex;
        dateNoted = builder.dateNoted;
    }

    public String getProductCodeOld() {
        return productCodeOld;
    }

    public String getContractNumberOld() {
        return contractNumberOld;
    }

    public String getProductCodeNew() {
        return productCodeNew;
    }

    public String getContractNumberNew() {
        return contractNumberNew;
    }

    public String getMovementOfTheDayIndex() {
        return movementOfTheDayIndex;
    }

    public String getDateNoted() {
        return dateNoted;
    }

    public static class Builder {
        private String productCodeOld;
        private String contractNumberOld;
        private String productCodeNew;
        private String contractNumberNew;
        private String movementOfTheDayIndex;
        private String dateNoted;

        public Builder withProductCodeOld(String productCodeOld) {
            this.productCodeOld = productCodeOld;
            return this;
        }

        public Builder withContractNumberOld(String contractNumberOld) {
            this.contractNumberOld = contractNumberOld;
            return this;
        }

        public Builder withProductCodeNew(String productCodeNew) {
            this.productCodeNew = productCodeNew;
            return this;
        }

        public Builder withContractNumberNew(String contractNumberNew) {
            this.contractNumberNew = contractNumberNew;
            return this;
        }

        public Builder withMovementOfTheDayIndex(String movementOfTheDayIndex) {
            this.movementOfTheDayIndex = movementOfTheDayIndex;
            return this;
        }

        public Builder withDateNoted(String dateNoted) {
            this.dateNoted = dateNoted;
            return this;
        }

        public AccountTransactionDetailsRequestQueryParams build() {
            return new AccountTransactionDetailsRequestQueryParams(this);
        }
    }
}
