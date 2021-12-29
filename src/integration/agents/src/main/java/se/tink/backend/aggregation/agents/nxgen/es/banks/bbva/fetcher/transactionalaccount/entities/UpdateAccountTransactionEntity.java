package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UpdateAccountTransactionEntity {

    private UpdateTransactionsAccountEntity account;

    private UpdateTransactionsContractEntity contract;

    public UpdateTransactionsAccountEntity getAccount() {
        return account;
    }

    public UpdateTransactionsContractEntity getContract() {
        return contract;
    }

    public static class Builder {

        private UpdateTransactionsAccountEntity account;
        private UpdateTransactionsContractEntity contract;

        public UpdateAccountTransactionEntity.Builder withAccount(
                UpdateTransactionsAccountEntity account) {
            this.account = account;
            return this;
        }

        public UpdateAccountTransactionEntity.Builder withContract(
                UpdateTransactionsContractEntity contract) {
            this.contract = contract;
            return this;
        }

        public UpdateAccountTransactionEntity build() {
            UpdateAccountTransactionEntity accountTransaction =
                    new UpdateAccountTransactionEntity();
            accountTransaction.account = account;
            accountTransaction.contract = contract;
            return accountTransaction;
        }
    }
}
