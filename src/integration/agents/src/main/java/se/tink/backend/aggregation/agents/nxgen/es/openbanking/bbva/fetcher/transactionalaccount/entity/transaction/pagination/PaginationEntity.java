package se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.fetcher.transactionalaccount.entity.transaction.pagination;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaginationEntity {
    private Integer page;
    private Integer numPages;
    private Integer pageSize;
    private Integer total;
    private NavigationLinksEntity links;

    public Integer getPage() {
        return page;
    }

    public Integer getNumPages() {
        return numPages;
    }
}
