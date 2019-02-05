package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto;

import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeEncValueTuple;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsHistoryRequest {
    private final TypeEncValueTuple companyNo;
    private final TypeEncValueTuple currency;
    private final TypeEncValueTuple accountNo;
    private final TypeEncValueTuple roleCode;
    private final TypeEncValueTuple repositioningKey;
    private final TypeValuePair transactionsQuantity;
    private final TypeValuePair searchMessage;
    private final TypeValuePair searchAmount;

    public TransactionsHistoryRequest(
            TypeEncValueTuple companyNo,
            TypeEncValueTuple currency,
            TypeEncValueTuple accountNo,
            TypeEncValueTuple roleCode,
            TypeEncValueTuple repositioningKey,
            TypeValuePair transactionsQuantity,
            TypeValuePair searchMessage,
            TypeValuePair searchAmount) {
        this.companyNo = companyNo;
        this.currency = currency;
        this.accountNo = accountNo;
        this.roleCode = roleCode;
        this.repositioningKey = repositioningKey;
        this.transactionsQuantity = transactionsQuantity;
        this.searchMessage = searchMessage;
        this.searchAmount = searchAmount;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private TypeEncValueTuple companyNo;
        private TypeEncValueTuple currency;
        private TypeEncValueTuple accountNo;
        private TypeEncValueTuple roleCode;
        private TypeEncValueTuple repositioningKey;
        private TypeValuePair transactionsQuantity;
        private TypeValuePair searchMessage;
        private TypeValuePair searchAmount;

        public Builder setCompanyNo(TypeEncValueTuple companyNo) {
            this.companyNo = companyNo;
            return this;
        }

        public Builder setCurrency(TypeEncValueTuple currency) {
            this.currency = currency;
            return this;
        }
        
        public Builder setAccountNo(TypeEncValueTuple accountNo) {
            this.accountNo = accountNo;
            return this;
        }

        public Builder setRoleCode(TypeEncValueTuple roleCode) {
            this.roleCode = roleCode;
            return this;
        }

        public Builder setRepositioningKey(TypeEncValueTuple repositioningKey) {
            this.repositioningKey = repositioningKey;
            return this;
        }

        public Builder setTransactionsQuantity(int transactionsQuantity) {
            this.transactionsQuantity =
                    TypeValuePair.create(KbcConstants.PairTypeTypes.SHORT, String.valueOf(transactionsQuantity));
            return this;
        }

        public Builder setSearchMessage(String searchMessage) {
            this.searchMessage = TypeValuePair.createText(searchMessage);
            return this;
        }

        public Builder setSearchAmount(String searchAmount) {
            this.searchAmount = TypeValuePair.createText(searchAmount);
            return this;
        }

        public TransactionsHistoryRequest build() {
            return new TransactionsHistoryRequest(companyNo, currency, accountNo, roleCode, repositioningKey,
                    transactionsQuantity, searchMessage, searchAmount);
        }
    }
}
