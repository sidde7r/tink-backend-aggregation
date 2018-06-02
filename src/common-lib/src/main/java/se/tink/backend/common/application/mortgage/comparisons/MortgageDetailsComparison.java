package se.tink.backend.common.application.mortgage.comparisons;

import java.text.NumberFormat;
import se.tink.backend.common.application.mortgage.CurrentMortgage;
import se.tink.backend.common.application.mortgage.MortgageProductUtils;
import se.tink.backend.common.dao.ProviderDao;
import se.tink.backend.common.utils.CurrencyFormatter;
import se.tink.backend.core.product.ProductArticle;

public class MortgageDetailsComparison {
    private final CurrentMortgage currentMortgage;
    private final ProductArticle newMortgageProduct;
    private final CurrencyFormatter currencyFormatter;
    private final NumberFormat percentFormatter;
    private final MortgageProductUtils mortgageProductUtils;
    private final ProviderDao providerDao;

    public MortgageDetailsComparison(
            CurrentMortgage currentMortgage, ProductArticle newMortgageProduct, CurrencyFormatter currencyFormatter,
            NumberFormat percentFormatter, ProviderDao providerDao) {
        this.currentMortgage = currentMortgage;
        this.newMortgageProduct = newMortgageProduct;
        this.currencyFormatter = currencyFormatter;
        this.percentFormatter = percentFormatter;
        this.mortgageProductUtils = new MortgageProductUtils(newMortgageProduct);
        this.providerDao = providerDao;
    }

    public double getCurrentAmount() {
        return currentMortgage.getAmount();
    }

    public String getCurrentProviderName() {
        return currentMortgage.getProviderName();
    }

    public String getCurrentProviderDisplayName() {
        return providerDao.getProvidersByName().get(getCurrentProviderName()).getDisplayName();
    }

    public double getCurrentInterestRate() {
        return currentMortgage.getInterestRate();
    }

    public String getCurrentInterestRateAsPercent() {
        return percentFormatter.format(getCurrentInterestRate());
    }

    private double getCurrentYearlyCost() {
        return currentMortgage.getAmount() * getCurrentInterestRate();
    }

    private double getCurrentMonthlyCost() {
        return getCurrentYearlyCost() / 12d;
    }

    public String getCurrentMonthlyCostCurrencyFormatted() {
        return currencyFormatter.formatCurrencyRound(getCurrentMonthlyCost());
    }

    public String getNewProviderName() {
        return newMortgageProduct.getProviderName();
    }

    public String getNewProviderDisplayName() {
        return providerDao.getProvidersByName().get(getNewProviderName()).getDisplayName();
    }

    public double getNewInterestRate() {
        return mortgageProductUtils.getInterestRateIncludingDiscount();
    }

    public String getNewInterestRateAsPercent() {
        return percentFormatter.format(getNewInterestRate());
    }

    private double getNewYearlyCost() {
        return currentMortgage.getAmount() * getNewInterestRate();
    }

    private double getNewMonthlyCost() {
        return getNewYearlyCost() / 12d;
    }

    public String getNewMonthlyCostCurrencyFormatted() {
        return currencyFormatter.formatCurrencyRound(getNewMonthlyCost());
    }

    /**
     * @return > 0 if the new mortgage costs less than current mortgage. < 0 if new mortgage costs more.
     */
    private double getYearlyProfit() {
        return getCurrentYearlyCost() - getNewYearlyCost();
    }

    private double getYearlyProfitAbs() {
        return Math.abs(getYearlyProfit());
    }

    public String getYearlyProfitAbsCurrencyFormatted() {
        return currencyFormatter.formatCurrencyRound(getYearlyProfitAbs());
    }

    /**
     * @return > 0 if the new mortgage costs less than current mortgage. < 0 if new mortgage costs more.
     */
    public double getMonthlyProfit() {
        return getCurrentMonthlyCost() - getNewMonthlyCost();
    }

    private double getMonthlyProfitAbs() {
        return Math.abs(getNewMonthlyCost() - getCurrentMonthlyCost());
    }

    public String getMonthlyProfitAbsCurrencyFormatted() {
        return currencyFormatter.formatCurrencyRound(getMonthlyProfitAbs());
    }

    private double getNewYearlyCostAfterTaxDeduction() {
        return getNewYearlyCost() * 0.7;
    }

    public String getNewYearlyCostAfterTaxDeductionCurrencyFormatted() {
        return currencyFormatter.formatCurrencyRound(getNewYearlyCostAfterTaxDeduction());
    }

    private double getNewMonthlyCostAfterTaxDeduction() {
        return getNewYearlyCostAfterTaxDeduction() / 12d;
    }

    public String getNewMonthlyCostAfterTaxDeductionCurrencyFormatted() {
        return currencyFormatter.formatCurrencyRound(getNewMonthlyCostAfterTaxDeduction());
    }

    public boolean isProfitable() {
        return getMonthlyProfit() >= 0;
    }
}
