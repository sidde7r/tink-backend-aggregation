package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditCardTransactionsPaginationEntity {
    private String firstPage;
    private String lastPage;
    private String pageSize;
    private String nextPage;

    public String getNextPage() {
        return nextPage;
    }
}
