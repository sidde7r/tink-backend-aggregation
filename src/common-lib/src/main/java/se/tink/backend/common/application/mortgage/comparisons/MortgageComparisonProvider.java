package se.tink.backend.common.application.mortgage.comparisons;

public class MortgageComparisonProvider {
    private String name;
    private double averageInterestRate;
    private double listInterestRate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAverageInterestRate() {
        return averageInterestRate;
    }

    public void setAverageInterestRate(double averageInterestRate) {
        this.averageInterestRate = averageInterestRate;
    }

    public double getListInterestRate() {
        return listInterestRate;
    }

    public void setListInterestRate(double listInterestRate) {
        this.listInterestRate = listInterestRate;
    }
}
