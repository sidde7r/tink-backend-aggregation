package se.tink.backend.common.mail.monthly.summary.utils;

import com.google.common.collect.ImmutableMap;

import se.tink.backend.categorization.api.SECategories;

public class IconsUtil {

    private static final ImmutableMap<String, String> CATEGORY_ICON_NAMES_BY_CODE = ImmutableMap
            .<String, String> builder()
            .put(SECategories.Codes.EXPENSES_FOOD, "fooddrinks.png")
            .put(SECategories.Codes.EXPENSES_WELLNESS, "health.png")
            .put(SECategories.Codes.EXPENSES_HOME, "home.png")
            .put(SECategories.Codes.EXPENSES_HOUSE, "house.png")
            .put(SECategories.Codes.EXPENSES_ENTERTAINMENT, "leisure.png")
            .put(SECategories.Codes.EXPENSES_MISC, "other.png")
            .put(SECategories.Codes.EXPENSES_TRANSPORT, "transport.png")
            .put(SECategories.Codes.EXPENSES_SHOPPING, "shopping.png")
            .build();

    public static String getIcon(String code){
        return CATEGORY_ICON_NAMES_BY_CODE.get(code);
    }

    public static String getSearchIcon(){
        return "search.png";
    }

}
