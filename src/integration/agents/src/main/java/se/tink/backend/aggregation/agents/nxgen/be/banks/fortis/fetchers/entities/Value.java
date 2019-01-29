package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

import java.util.Collection;
import java.util.List;

@JsonObject
public class Value {
    private List<ViewListItem> viewList;

    public List<ViewListItem> getViewList() {
        return viewList;
    }

    public Collection<TransactionalAccount> toTinkAccounts() {
        return viewList.get(0).toTinkAccounts();
    }
}
