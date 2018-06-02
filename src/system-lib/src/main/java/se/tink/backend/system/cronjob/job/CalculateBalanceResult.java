package se.tink.backend.system.cronjob.job;

public class CalculateBalanceResult {

    private int usersWithWrongBalance;
    private double totalBalanceDiff;
    private double highestBalanceDiffGauge;

    public int getUsersWithWrongBalance() {
        return usersWithWrongBalance;
    }

    public void setUsersWithWrongBalance(int usersWithWrongBalance) {
        this.usersWithWrongBalance = usersWithWrongBalance;
    }

    public double getTotalBalanceDiff() {
        return totalBalanceDiff;
    }

    public void setTotalBalanceDiff(double totalBalanceDiff) {
        this.totalBalanceDiff = totalBalanceDiff;
    }

    public double getHighestBalanceDiffGauge() {
        return highestBalanceDiffGauge;
    }

    public void setHighestBalanceDiffGauge(double highestBalanceDiffGauge) {
        this.highestBalanceDiffGauge = highestBalanceDiffGauge;
    }
}
