package se.tink.libraries.creditsafe.consumermonitoring.api;

import java.util.List;

public class PageableConsumerCreditSafeResponse extends CreditSafeResponse {
    private List<String> consumers;
    private int pageEnd;
    private int totalPortfolioSize;

    public List<String> getConsumers() {
        return consumers;
    }

    public void setConsumers(List<String> consumers) {
        this.consumers = consumers;
    }

    public int getPageEnd() {
        return pageEnd;
    }

    public void setPageEnd(int pageEnd) {
        this.pageEnd = pageEnd;
    }

    public int getTotalPortfolioSize() {
        return totalPortfolioSize;
    }

    public void setTotalPortfolioSize(int totalPortfolioSize) {
        this.totalPortfolioSize = totalPortfolioSize;
    }
}
