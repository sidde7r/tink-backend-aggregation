package se.tink.backend.common.mail.monthly.summary.model;

public class ActivityData {

    private int lowBalanceCount;
    private int largeExpenseCount;
    private int moreThanUsualCount;

    public void setLowBalanceCount(int lowBalanceCount) {
        this.lowBalanceCount = lowBalanceCount;
    }

    public int getLowBalanceCount() {
        return lowBalanceCount;
    }

    public void setLargeExpenseCount(int largeExpenseCount) {
        this.largeExpenseCount = largeExpenseCount;
    }

    public int getLargeExpenseCount() {
        return largeExpenseCount;
    }

    public void setMoreThanUsualCount(int moreThanUsualCount) {
        this.moreThanUsualCount = moreThanUsualCount;
    }

    public int getMoreThanUsualCount() {
        return moreThanUsualCount;
    }
}

