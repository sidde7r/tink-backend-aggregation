package se.tink.backend.common.application;

import com.google.common.base.Strings;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import se.tink.backend.core.FraudAddressContent;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.FraudRealEstateEngagementContent;

public class PropertyUtils {

    private static final Locale SWEDISH_LOCALE = new Locale("sv", "SE");
    private static final Pattern APARTMENT_NUMBER_IN_ADDRESS_PATTERN = Pattern.compile("\\slgh\\s?\\d{4}$",
            Pattern.CASE_INSENSITIVE); // Götgatan 12 Lgh 1234
    private static final Pattern FLOOR_NUMBER_IN_ADDRESS_PATTERN = Pattern.compile("\\s\\d+\\s?tr$",
            Pattern.CASE_INSENSITIVE); // Götgatan 12 1 tr

    public static String cleanStreetAddress(String streetAddress) {
        if (Strings.isNullOrEmpty(streetAddress)) {
            return streetAddress;
        }

        streetAddress = FLOOR_NUMBER_IN_ADDRESS_PATTERN.matcher(streetAddress).replaceAll("");
        streetAddress = APARTMENT_NUMBER_IN_ADDRESS_PATTERN.matcher(streetAddress).replaceAll("");
        return streetAddress;
    }
    
    public static boolean isApartment(FraudAddressContent address,
            ListMultimap<FraudDetailsContentType, FraudDetails> fraudDetails) {
        return isApartment(address, getRealEstateEngagements(fraudDetails));
    }

    private static List<FraudRealEstateEngagementContent> getRealEstateEngagements(
            ListMultimap<FraudDetailsContentType, FraudDetails> fraudDetailsByType) {
        List<FraudDetails> fraudDetails = fraudDetailsByType.get(FraudDetailsContentType.REAL_ESTATE_ENGAGEMENT);

        if (fraudDetails.isEmpty()) {
            return Collections.emptyList();
        }

        List<FraudRealEstateEngagementContent> realEstateEngagements = Lists.newArrayList();

        for (FraudDetails fraudDetail : fraudDetails) {
            realEstateEngagements.add((FraudRealEstateEngagementContent) fraudDetail.getContent());
        }

        return realEstateEngagements;
    }

    public static boolean isApartment(FraudAddressContent address,
            List<FraudRealEstateEngagementContent> engagements){
        if (address == null || Strings.isNullOrEmpty(address.getAddress())) {
            return false;
        }

        if (APARTMENT_NUMBER_IN_ADDRESS_PATTERN.matcher(address.getAddress()).find()) {
            return true;
        }

        if (FLOOR_NUMBER_IN_ADDRESS_PATTERN.matcher(address.getAddress()).find()) {
            return true;
        }

        // By having any engagements with matching municipality we guess it's a house that the user owns
        for (FraudRealEstateEngagementContent engagement : engagements) {
            if (hasMatchingMunicipality(engagement, address)) {
                return false;
            }
        }

        return true;
    }

    private static boolean hasMatchingMunicipality(FraudRealEstateEngagementContent engagement,
            FraudAddressContent address) {
        String engagementMunicipality = engagement.getMuncipality();

        if (Strings.isNullOrEmpty(engagementMunicipality)) {
            return false;
        }

        engagementMunicipality = engagementMunicipality.toLowerCase(SWEDISH_LOCALE);

        String community = address.getCommunity();
        if (!Strings.isNullOrEmpty(community)) {
            if (community.toLowerCase(SWEDISH_LOCALE).equals(engagementMunicipality)) {
                return true;
            }
        }

        String city = address.getCity();
        if (!Strings.isNullOrEmpty(city)) {
            if (city.toLowerCase(SWEDISH_LOCALE).equals(engagementMunicipality)) {
                return true;
            }
        }

        return false;
    }
}
