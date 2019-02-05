package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto;

import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FutureTransactionsRequest {
    private TypeValuePair currencyCode;
    private TypeValuePair accountNo;
    private TypeValuePair repositioningKey;
    private TypeValuePair transactionsQuantity;

    public FutureTransactionsRequest(TypeValuePair currencyCode,
            TypeValuePair accountNo,
            TypeValuePair repositioningKey,
            TypeValuePair transactionsQuantity) {
        this.currencyCode = currencyCode;
        this.accountNo = accountNo;
        this.repositioningKey = repositioningKey;
        this.transactionsQuantity = transactionsQuantity;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private TypeValuePair currencyCode;
        private TypeValuePair accountNo;
        private TypeValuePair repositioningKey;
        private TypeValuePair transactionsQuantity;

        public Builder setCurrencyCode(String currencyCode) {
            this.currencyCode = TypeValuePair.createText(currencyCode);
            return this;
        }

        public Builder setAccountNo(String accountNo) {
            this.accountNo = TypeValuePair.create(KbcConstants.PairTypeTypes.IBAN, accountNo);
            return this;
        }

        public Builder setRepositioningKey(String repositioningKey) {
            this.repositioningKey = TypeValuePair.createText(repositioningKey);
            return this;
        }

        public Builder setTransactionsQuantity(int transactionsQuantity) {
            this.transactionsQuantity =
                    TypeValuePair.create(KbcConstants.PairTypeTypes.SHORT, String.valueOf(transactionsQuantity));
            return this;
        }

        public FutureTransactionsRequest build() {
            return new FutureTransactionsRequest(currencyCode, accountNo, repositioningKey, transactionsQuantity);
        }
    }
}
