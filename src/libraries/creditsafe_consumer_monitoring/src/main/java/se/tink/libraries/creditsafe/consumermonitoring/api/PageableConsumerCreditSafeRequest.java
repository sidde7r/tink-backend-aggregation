package se.tink.libraries.creditsafe.consumermonitoring.api;

public class PageableConsumerCreditSafeRequest {
    private String portfolio;
    private int pageStart;
    private int pageSize;

    public PageableConsumerCreditSafeRequest() {

    }

    public PageableConsumerCreditSafeRequest(String portfolio, int size, int start) {
        this.portfolio = portfolio;
        this.pageSize = size;
        this.pageStart = start;
    }

    public String getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(String portfolio) {
        this.portfolio = portfolio;
    }

    public int getPageStart() {
        return pageStart;
    }

    public void setPageStart(int pageStart) {
        this.pageStart = pageStart;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
