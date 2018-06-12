package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaginatorEntity {
    private int page;
    private int itemsPerPage;
    private String order;
    private int totalItems;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getItemsPerPage() {
        return itemsPerPage;
    }

    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }
}
