package se.tink.backend.system.document.core;

public class TaxReport {

    private final Double yearlySalary;

    public TaxReport(Double yearlySalary) {
        this.yearlySalary = yearlySalary;
    }

    public Double getYearlySalary() {
        return yearlySalary;
    }
}
