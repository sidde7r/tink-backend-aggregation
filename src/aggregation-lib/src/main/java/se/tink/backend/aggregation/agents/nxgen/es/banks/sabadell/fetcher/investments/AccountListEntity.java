package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.entities.PaginatorEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountListEntity {
    private List<Object> accounts;
    private PaginatorEntity paginator;

    public List<Object> getAccounts() {
        return accounts;
    }

    public PaginatorEntity getPaginator() {
        return paginator;
    }
}
