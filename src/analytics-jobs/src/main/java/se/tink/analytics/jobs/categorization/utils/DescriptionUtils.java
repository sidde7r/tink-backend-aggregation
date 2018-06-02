package se.tink.analytics.jobs.categorization.utils;

import com.google.common.base.CharMatcher;
import java.util.Map;
import se.tink.backend.core.Cities;
import se.tink.backend.core.Market;
import se.tink.backend.utils.CategorizationUtils;

public class DescriptionUtils {

    public static String cleanDescription(String description, Map<String, Cities> citiesByMarket, String market) {

        String cleanDescription = description;

        if (Market.Code.SE.name().equalsIgnoreCase(market)) {
            cleanDescription = CategorizationUtils.removeSwedishPersonalIdentityNumber(cleanDescription);
        }

        if (Market.Code.NL.name().equalsIgnoreCase(market)) {
            cleanDescription = CategorizationUtils.removeDutchCardNumber(cleanDescription);
        }

        cleanDescription = CategorizationUtils.clean(cleanDescription);
        cleanDescription = CategorizationUtils.trimNumbers(cleanDescription);

        Cities cities = citiesByMarket.get(market);

        if (cities != null) {
            cleanDescription = cities.trimFuzzy(cleanDescription);
            cleanDescription = cities.trim(cleanDescription);
            cleanDescription = CategorizationUtils.trimNumbers(cleanDescription);
        }

        cleanDescription = CharMatcher.WHITESPACE.trimAndCollapseFrom(cleanDescription, ' ');

        return cleanDescription;
    }
}
