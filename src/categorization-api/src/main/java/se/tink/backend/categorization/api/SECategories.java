package se.tink.backend.categorization.api;

import com.google.common.collect.Sets;
import java.util.Set;

public class SECategories implements CategoryConfiguration {

    public static class Codes {
        public static final String EXPENSES_HOME = "expenses:home";
        public static final String EXPENSES_HOME_RENT = "expenses:home.rent";
        public static final String EXPENSES_HOME_MORTGAGE = "expenses:home.mortgage";
        public static final String EXPENSES_HOME_COMMUNICATIONS = "expenses:home.communications";
        public static final String EXPENSES_HOME_UTILITIES = "expenses:home.utilities";
        public static final String EXPENSES_HOME_INCURENCES_FEES = "expenses:home.incurences-fees"; // Typo. Should be `expenses:home.insurances-fees`.
        public static final String EXPENSES_HOME_SERVICES = "expenses:home.services";
        public static final String EXPENSES_HOME_OTHER = "expenses:home.other";
        public static final String EXPENSES_HOUSE = "expenses:house";
        public static final String EXPENSES_HOUSE_REPAIRS = "expenses:house.repairs";
        public static final String EXPENSES_HOUSE_FITMENT = "expenses:house.fitment";
        public static final String EXPENSES_HOUSE_GARDEN = "expenses:house.garden";
        public static final String EXPENSES_HOUSE_OTHER = "expenses:house.other";
        public static final String EXPENSES_FOOD = "expenses:food";
        public static final String EXPENSES_FOOD_GROCERIES = "expenses:food.groceries";
        public static final String EXPENSES_FOOD_RESTAURANTS = "expenses:food.restaurants";
        public static final String EXPENSES_FOOD_COFFEE = "expenses:food.coffee";
        public static final String EXPENSES_FOOD_ALCOHOL_TOBACCO = "expenses:food.alcohol-tobacco";
        public static final String EXPENSES_FOOD_BARS = "expenses:food.bars";
        public static final String EXPENSES_FOOD_OTHER = "expenses:food.other";
        public static final String EXPENSES_TRANSPORT = "expenses:transport";
        public static final String EXPENSES_TRANSPORT_CAR = "expenses:transport.car";
        public static final String EXPENSES_TRANSPORT_PUBLICTRANSPORT = "expenses:transport.publictransport";
        public static final String EXPENSES_TRANSPORT_FLIGHTS = "expenses:transport.flights";
        public static final String EXPENSES_TRANSPORT_TAXI = "expenses:transport.taxi";
        public static final String EXPENSES_TRANSPORT_OTHER = "expenses:transport.other";
        public static final String EXPENSES_SHOPPING = "expenses:shopping";
        public static final String EXPENSES_SHOPPING_CLOTHES = "expenses:shopping.clothes";
        public static final String EXPENSES_SHOPPING_ELECTRONICS = "expenses:shopping.electronics";
        public static final String EXPENSES_SHOPPING_HOBBY = "expenses:shopping.hobby";
        public static final String EXPENSES_SHOPPING_BOOKS = "expenses:shopping.books";
        public static final String EXPENSES_SHOPPING_GIFTS = "expenses:shopping.gifts";
        public static final String EXPENSES_SHOPPING_OTHER = "expenses:shopping.other";
        public static final String EXPENSES_ENTERTAINMENT = "expenses:entertainment";
        public static final String EXPENSES_ENTERTAINMENT_CULTURE = "expenses:entertainment.culture";
        public static final String EXPENSES_ENTERTAINMENT_HOBBY = "expenses:entertainment.hobby";
        public static final String EXPENSES_ENTERTAINMENT_SPORT = "expenses:entertainment.sport";
        public static final String EXPENSES_ENTERTAINMENT_VACATION = "expenses:entertainment.vacation";
        public static final String EXPENSES_ENTERTAINMENT_OTHER = "expenses:entertainment.other";
        public static final String EXPENSES_WELLNESS = "expenses:wellness";
        public static final String EXPENSES_WELLNESS_HEALTHCARE = "expenses:wellness.healthcare";
        public static final String EXPENSES_WELLNESS_PHARMACY = "expenses:wellness.pharmacy";
        public static final String EXPENSES_WELLNESS_EYECARE = "expenses:wellness.eyecare";
        public static final String EXPENSES_WELLNESS_BEAUTY = "expenses:wellness.beauty";
        public static final String EXPENSES_WELLNESS_OTHER = "expenses:wellness.other";
        public static final String EXPENSES_MISC = "expenses:misc";
        public static final String EXPENSES_MISC_EDUCATION = "expenses:misc.education";
        public static final String EXPENSES_MISC_WITHDRAWALS = "expenses:misc.withdrawals";
        public static final String EXPENSES_MISC_OUTLAYS = "expenses:misc.outlays";
        public static final String EXPENSES_MISC_KIDS = "expenses:misc.kids";
        public static final String EXPENSES_MISC_PETS = "expenses:misc.pets";
        public static final String EXPENSES_MISC_CHARITY = "expenses:misc.charity";
        public static final String EXPENSES_MISC_UNCATEGORIZED = "expenses:misc.uncategorized";
        public static final String EXPENSES_MISC_OTHER = "expenses:misc.other";
        public static final String INCOME_SALARY = "income:salary";
        public static final String INCOME_SALARY_OTHER = "income:salary.other";
        public static final String INCOME_PENSION = "income:pension";
        public static final String INCOME_PENSION_OTHER = "income:pension.other";
        public static final String INCOME_REFUND = "income:refund";
        public static final String INCOME_REFUND_OTHER = "income:refund.other";
        public static final String INCOME_BENEFITS = "income:benefits";
        public static final String INCOME_BENEFITS_OTHER = "income:benefits.other";
        public static final String INCOME_FINANCIAL = "income:financial";
        public static final String INCOME_FINANCIAL_OTHER = "income:financial.other";
        public static final String INCOME_OTHER = "income:other";
        public static final String INCOME_OTHER_OTHER = "income:other.other";
        public static final String TRANSFERS_SAVINGS = "transfers:savings";
        public static final String TRANSFERS_SAVINGS_OTHER = "transfers:savings.other";
        public static final String TRANSFERS_OTHER = "transfers:other";
        public static final String TRANSFERS_OTHER_OTHER = "transfers:other.other";
        public static final String TRANSFERS_EXCLUDE = "transfers:exclude";
        public static final String TRANSFERS_EXCLUDE_OTHER = "transfers:exclude.other";
    }

    @Override
    public Set<String> getMerchantizeCodes() {
        return Sets.newHashSet(Codes.EXPENSES_FOOD_RESTAURANTS, Codes.EXPENSES_FOOD_GROCERIES, Codes.EXPENSES_FOOD_COFFEE,
                Codes.EXPENSES_FOOD_BARS, Codes.EXPENSES_FOOD_ALCOHOL_TOBACCO, Codes.EXPENSES_SHOPPING_CLOTHES,
                Codes.EXPENSES_SHOPPING_ELECTRONICS, Codes.EXPENSES_ENTERTAINMENT_HOBBY);
    }

    @Override
    public Set<String> getUnusualActivityExcludedCodes() {
        return Sets.newHashSet(Codes.EXPENSES_MISC_UNCATEGORIZED, Codes.EXPENSES_MISC_OTHER, Codes.EXPENSES_MISC_OUTLAYS);
    }

    @Override
    public Set<String> getMonthlySummaryActivityExcludedCodes() {
        return Sets.newHashSet(Codes.EXPENSES_HOME_MORTGAGE, Codes.EXPENSES_HOME_RENT);
    }

    @Override
    public Set<String> getWeeklySummaryActivityExcludedCodes() {
        return Sets.newHashSet(Codes.EXPENSES_HOME_MORTGAGE, Codes.EXPENSES_HOME_RENT);
    }

    @Override
    public Set<String> getDoubleChargeActivityExcludedCodes() {
        return Sets.newHashSet(Codes.EXPENSES_HOME_MORTGAGE, Codes.EXPENSES_HOME_RENT);
    }

    @Override
    public Set<String> getHeatmapActivityCodes() {
        return Sets.newHashSet(Codes.EXPENSES_FOOD_BARS, Codes.EXPENSES_FOOD_COFFEE, Codes.EXPENSES_FOOD_RESTAURANTS);
    }

    @Override
    public Set<String> getSuggestExpensesFollowCodes() {
        return Sets.newHashSet(Codes.EXPENSES_FOOD_GROCERIES, Codes.EXPENSES_FOOD_ALCOHOL_TOBACCO, Codes.EXPENSES_FOOD_COFFEE, Codes.EXPENSES_FOOD_RESTAURANTS, Codes.EXPENSES_TRANSPORT_TAXI, Codes.EXPENSES_SHOPPING_CLOTHES);
    }

    @Override
    public Set<String> getIncomeCodes() {
        return Sets.newHashSet(Codes.INCOME_BENEFITS_OTHER, Codes.INCOME_PENSION_OTHER, Codes.INCOME_SALARY_OTHER);
    }

    @Override
    public String getExpenseUnknownCode() {
        return Codes.EXPENSES_MISC_UNCATEGORIZED;
    }

    @Override
    public String getIncomeUnknownCode() {
        return Codes.INCOME_OTHER_OTHER;
    }

    @Override
    public String getTransferUnknownCode() {
        return Codes.TRANSFERS_OTHER_OTHER;
    }

    @Override public String getServicesCode() {
        return Codes.EXPENSES_HOME_SERVICES;
    }

    @Override public String getMortgageCode() {
        return Codes.EXPENSES_HOME_MORTGAGE;
    }

    @Override public String getRentCode() {
        return Codes.EXPENSES_HOME_RENT;
    }

    @Override public String getRestaurantsCode() {
        return Codes.EXPENSES_FOOD_RESTAURANTS;
    }

    @Override public String getCoffeeCode() {
        return Codes.EXPENSES_FOOD_COFFEE;
    }

    @Override public String getGroceriesCode() {
        return Codes.EXPENSES_FOOD_GROCERIES;
    }

    @Override public String getBarsCode() {
        return Codes.EXPENSES_FOOD_BARS;
    }

    @Override public String getAlcoholTobaccoCode() {
        return Codes.EXPENSES_FOOD_ALCOHOL_TOBACCO;
    }

    @Override public String getFoodOtherCode() {
        return Codes.EXPENSES_FOOD_OTHER;
    }

    @Override public String getTaxiCode() {
        return Codes.EXPENSES_TRANSPORT_TAXI;
    }

    @Override public String getClothesCode() {
        return Codes.EXPENSES_SHOPPING_CLOTHES;
    }

    @Override public String getElectronicsCode() {
        return Codes.EXPENSES_SHOPPING_ELECTRONICS;
    }

    @Override public String getShoppingHobbyCode() {
        return Codes.EXPENSES_SHOPPING_HOBBY;
    }

    @Override public String getVacationCode() {
        return Codes.EXPENSES_ENTERTAINMENT_VACATION;
    }

    @Override public String getWithdrawalsCode() {
        return Codes.EXPENSES_MISC_WITHDRAWALS;
    }

    @Override public String getSalaryCode() {
        return Codes.INCOME_SALARY_OTHER;
    }

    @Override public String getSavingsCode() {
        return Codes.TRANSFERS_SAVINGS_OTHER;
    }

    @Override public String getExcludeCode() {
        return Codes.TRANSFERS_EXCLUDE_OTHER;
    }

    @Override public String getRefundCode() {
        return Codes.INCOME_REFUND_OTHER;
    }
}
