package se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.fetcher.transactionalaccount.entity.transaction.pagination;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NavigationLinksEntity {

    private NavigationLinkEntity firstPage;
    private NavigationLinkEntity lastPage;
    private NavigationLinkEntity next;
    private NavigationLinkEntity prev;
}
