package se.tink.backend.common.application.mortgage;

public class CurrentLoan {

    private double amount;
    private String lender;

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getLender() {
        return lender;
    }

    public void setLender(String lender) {
        this.lender = lender;
    }
}
