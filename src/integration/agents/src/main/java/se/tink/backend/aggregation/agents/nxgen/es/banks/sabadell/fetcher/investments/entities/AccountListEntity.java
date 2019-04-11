package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.entities;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.entities.PaginatorEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountListEntity {
    private List<AccountEntity> accounts;
    private PaginatorEntity paginator;

    public List<AccountEntity> getAccounts() {
        return accounts;
    }

    public PaginatorEntity getPaginator() {
        return paginator;
    }
}
