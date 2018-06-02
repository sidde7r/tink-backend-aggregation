package se.tink.libraries.identity.model;

import java.util.Date;

public class TaxDeclaration {
    private Date createdDate;
    private double finalTax;
    private double totalIncome;
    private double incomeByService;
    private double incomeByCapital;
    private Integer year;
    private Date registeredDate;

    private TaxDeclaration(double finalTax, double totalIncome, double incomeByService, double incomeByCapital,
            Integer year, Date registeredDate, Date createdDate) {
        this.finalTax = finalTax;
        this.totalIncome = totalIncome;
        this.incomeByService = incomeByService;
        this.incomeByCapital = incomeByCapital;
        this.year = year;
        this.registeredDate = registeredDate;
        this.createdDate = createdDate;
    }

    public double getFinalTax() {
        return finalTax;
    }

    public double getTotalIncome() {
        return totalIncome;
    }

    public double getIncomeByCapital() {
        return incomeByCapital;
    }

    public Integer getYear() {
        return year;
    }

    public Date getRegisteredDate() {
        return registeredDate;
    }

    public static TaxDeclaration of(double finalTax, double totalIncome, double incomeByService, double incomeByCapital,
            Integer year, Date registeredDate, Date createdDate) {
        return new TaxDeclaration(finalTax, totalIncome, incomeByService, incomeByCapital, year, registeredDate,
                createdDate);
    }

    public double getIncomeByService() {
        return incomeByService;
    }

    public Date getCreatedDate() {
        return createdDate;
    }
}
