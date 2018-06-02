package se.tink.backend.core.application;

public class PreliminaryInterestRate {
    private Double interestRate;

    public static PreliminaryInterestRate of(Double interestRate) {
        PreliminaryInterestRate preliminaryInterestRate = new PreliminaryInterestRate();
        preliminaryInterestRate.interestRate = interestRate;
        return preliminaryInterestRate;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(Double interestRate) {
        this.interestRate = interestRate;
    }
}
