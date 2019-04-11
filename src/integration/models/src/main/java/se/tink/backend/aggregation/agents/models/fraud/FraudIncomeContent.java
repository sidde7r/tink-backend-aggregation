package se.tink.backend.aggregation.agents.models.fraud;

import java.util.Date;
import java.util.Objects;

public class FraudIncomeContent extends FraudDetailsContent {
    private double finalTax;
    private double totalIncome;
    private double incomeByService;
    private double incomeByCapital;
    private String taxYear;
    private Date registered;

    public double getFinalTax() {
        return finalTax;
    }

    public void setFinalTax(double finalTax) {
        this.finalTax = finalTax;
    }

    public double getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(double totalIncome) {
        this.totalIncome = totalIncome;
    }

    public double getIncomeByService() {
        return incomeByService;
    }

    public void setIncomeByService(double incomeByService) {
        this.incomeByService = incomeByService;
    }

    public double getIncomeByCapital() {
        return incomeByCapital;
    }

    public void setIncomeByCapital(double incomeByCapital) {
        this.incomeByCapital = incomeByCapital;
    }

    public Date getRegistered() {
        return registered;
    }

    public void setRegistered(Date registered) {
        this.registered = registered;
    }

    @Override
    public FraudTypes itemType() {
        return FraudTypes.INQUIRY;
    }

    @Override
    public String generateContentId() {
        return String.valueOf(Objects.hash(itemType(), totalIncome, taxYear));
    }

    public String getTaxYear() {
        return taxYear;
    }

    public void setTaxYear(String taxYear) {
        this.taxYear = taxYear;
    }
}
