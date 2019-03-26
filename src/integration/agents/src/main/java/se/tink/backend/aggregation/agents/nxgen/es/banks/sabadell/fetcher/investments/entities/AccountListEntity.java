package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.entities;

import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.entities.PaginatorEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

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
