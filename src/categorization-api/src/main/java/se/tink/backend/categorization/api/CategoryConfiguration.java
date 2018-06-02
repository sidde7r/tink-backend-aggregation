package se.tink.backend.categorization.api;

import java.util.Set;

public interface CategoryConfiguration {

    public Set<String> getMerchantizeCodes();

    public Set<String> getUnusualActivityExcludedCodes();

    public Set<String> getMonthlySummaryActivityExcludedCodes();

    public Set<String> getWeeklySummaryActivityExcludedCodes();

    public Set<String> getDoubleChargeActivityExcludedCodes();

    public Set<String> getHeatmapActivityCodes();

    public Set<String> getSuggestExpensesFollowCodes();

    public Set<String> getIncomeCodes();

    public String getExpenseUnknownCode();

    public String getIncomeUnknownCode();

    public String getTransferUnknownCode();

    public String getServicesCode();

    public String getMortgageCode();

    public String getRentCode();

    public String getRestaurantsCode();

    public String getCoffeeCode();

    public String getGroceriesCode();

    public String getBarsCode();

    public String getAlcoholTobaccoCode();

    public String getFoodOtherCode();

    public String getTaxiCode();

    public String getClothesCode();

    public String getElectronicsCode();

    public String getShoppingHobbyCode();

    public String getVacationCode();

    public String getWithdrawalsCode();

    public String getSalaryCode();

    public String getSavingsCode();

    public String getExcludeCode();

    String getRefundCode();
}
