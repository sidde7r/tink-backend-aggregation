package se.tink.libraries.creditsafe.consumermonitoring.api;

public class ChangedConsumerCreditSafeRequest extends PageableConsumerCreditSafeRequest {
    private int changedDays;

    public ChangedConsumerCreditSafeRequest() {
        super();
    }

    public ChangedConsumerCreditSafeRequest(
            String portfolio, int size, int start, int changedDays) {
        super(portfolio, size, start);
        this.changedDays = changedDays;
    }

    public int getChangedDays() {
        return changedDays;
    }

    public void setChangedDays(int changedDays) {
        this.changedDays = changedDays;
    }
}
