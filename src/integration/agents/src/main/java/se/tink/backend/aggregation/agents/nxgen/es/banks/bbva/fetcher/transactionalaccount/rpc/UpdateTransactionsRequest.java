package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.UpdateTransactionsContractEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UpdateTransactionsRequest {

    private List<UpdateTransactionsContractEntity> contracts;

    public List<UpdateTransactionsContractEntity> getContracts() {
        return contracts;
    }

    public static UpdateTransactionsRequestBuilder builder() {
        return new UpdateTransactionsRequestBuilder();
    }

    public static class UpdateTransactionsRequestBuilder {

        private List<UpdateTransactionsContractEntity> contracts;

        public UpdateTransactionsRequest.UpdateTransactionsRequestBuilder withContracts(
                List<UpdateTransactionsContractEntity> contracts) {
            this.contracts = contracts;
            return this;
        }

        public UpdateTransactionsRequest build() {
            UpdateTransactionsRequest updateTransactionsRequest = new UpdateTransactionsRequest();
            updateTransactionsRequest.contracts = contracts;
            return updateTransactionsRequest;
        }
    }
}
