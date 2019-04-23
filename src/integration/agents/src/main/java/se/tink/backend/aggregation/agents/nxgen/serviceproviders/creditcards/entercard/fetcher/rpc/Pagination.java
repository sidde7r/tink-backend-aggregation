package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@SuppressWarnings("unused")
@JsonObject
public class Pagination {

    private int page;
    private int perPage;
    private int total;

    public boolean canFetchMore() {
        return page * perPage < total;
    }
}
