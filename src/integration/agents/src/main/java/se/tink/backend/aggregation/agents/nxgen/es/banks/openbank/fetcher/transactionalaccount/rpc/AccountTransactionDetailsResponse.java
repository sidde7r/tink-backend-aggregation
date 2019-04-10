package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vavr.collection.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities.PaginationEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountTransactionDetailsResponse {
    @JsonProperty("_links")
    private PaginationEntity pagination;

    @JsonProperty("listaDetalle")
    private DetailsList detailsList;

    public PaginationEntity getPagination() {
        return pagination;
    }

    public List<String> getDescriptions() {
        return detailsList.transactionDescriptions;
    }

    private static class DetailsList {
        @JsonProperty("descMovimiento")
        private List<String> transactionDescriptions;
    }
}
