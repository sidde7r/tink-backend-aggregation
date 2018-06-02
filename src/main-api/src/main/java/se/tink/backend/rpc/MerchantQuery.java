package se.tink.backend.rpc;

import io.protostuff.Tag;

public class MerchantQuery {

    @Tag(1)
    private String id;
    @Tag(2)
    private int limit;
    @Tag(3)
    private String queryString;
    @Tag(4)
    private String reference;
    @Tag(5)
    private String transactionId;

    public String getId() {
        return id;
    }

    public int getLimit() {
        return limit;
    }

    public String getQueryString() {
        return queryString;
    }

    public String getReference() {
        return reference;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

}
