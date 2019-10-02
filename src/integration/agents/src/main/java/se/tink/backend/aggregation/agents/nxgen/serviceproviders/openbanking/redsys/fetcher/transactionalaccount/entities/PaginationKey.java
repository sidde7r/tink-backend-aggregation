package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.entities;

public class PaginationKey {
    private String path;
    private String requestId;

    public PaginationKey(String path, String requestId) {
        this.path = path;
        this.requestId = requestId;
    }

    public String getPath() {
        return path;
    }

    public String getRequestId() {
        return requestId;
    }
}
