package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto;

import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsHistoryRequest {
    private final TypeValuePair companyNo;
    private final TypeValuePair currency;
    private final TypeValuePair accountNo;
    private final TypeValuePair roleCode;
    private final TypeValuePair repositioningKey;
    private final TypeValuePair transactionsQuantity;
    private final TypeValuePair searchMessage;
    private final TypeValuePair searchAmount;

    public TransactionsHistoryRequest(
            TypeValuePair companyNo,
            TypeValuePair currency,
            TypeValuePair accountNo,
            TypeValuePair roleCode,
            TypeValuePair repositioningKey,
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
        private TypeValuePair companyNo;
        private TypeValuePair currency;
        private TypeValuePair accountNo;
        private TypeValuePair roleCode;
        private TypeValuePair repositioningKey;
        private TypeValuePair transactionsQuantity;
        private TypeValuePair searchMessage;
        private TypeValuePair searchAmount;

        public Builder setCompanyNo(String companyNo) {
            this.companyNo = TypeValuePair.createText(companyNo);
            return this;
        }

        public Builder setCurrency(String currency) {
            this.currency = TypeValuePair.createText(currency);
            return this;
        }

        public Builder setAccountNo(String accountNo) {
            this.accountNo = TypeValuePair.create(KbcConstants.PairTypeTypes.IBAN, accountNo);
            return this;
        }

        public Builder setRoleCode(String roleCode) {
            this.roleCode = TypeValuePair.createText(roleCode);
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
