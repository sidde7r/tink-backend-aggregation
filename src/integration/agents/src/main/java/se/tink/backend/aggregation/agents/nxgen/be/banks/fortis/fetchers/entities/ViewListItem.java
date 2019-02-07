package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@JsonObject
public class ViewListItem {
    private String viewId;
    private List<ViewDetailListItem> viewDetailList;
    private String viewName;
    private String viewType;
    private boolean flagDefault;

    public String getViewId() {
        return viewId;
    }

    public List<ViewDetailListItem> getViewDetailList() {
        return viewDetailList;
    }

    public String getViewName() {
        return viewName;
    }

    public String getViewType() {
        return viewType;
    }

    public boolean isFlagDefault() {
        return flagDefault;
    }

    public Collection<TransactionalAccount> toTinkAccounts() {
        return viewDetailList
                .stream()
                .filter(acc -> acc.isValid())
                .map(ViewDetailListItem::toTinkAccount)
                .collect(Collectors.toList());
    }
}
