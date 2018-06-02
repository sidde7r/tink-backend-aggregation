package se.tink.backend.common.merchants;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import se.tink.backend.core.Merchant;
import se.tink.backend.core.MerchantSources;
import se.tink.backend.core.Place;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.MerchantUtils;
import se.tink.backend.utils.StringUtils;

public class MerchantGoogleLocationEnricher {

    private static final LogUtils log = new LogUtils(MerchantGoogleLocationEnricher.class);
    private GooglePlacesSearcher searcher = new GooglePlacesSearcher();
    private Integer googleDelay = 500;

    private double addressSimilarityThreshold = 0.85;
    private double merchantNameSimilarityThreshold = 0.78;

    public List<Merchant> enrichMerchants(List<Merchant> merchants, String locale, String country) throws Exception {

        List<Merchant> updatedMerchants = new ArrayList<>();

        Integer count = 0;

        for (Merchant m : merchants) {

            count++;
            if (count % 10 == 0) {
                log.info(String.format("Have done %s of %s", count, merchants.size()));
            }

            if (shouldIgnoreMerchant(m)) {
                continue;
            }

            boolean merchantFound = tryCopyMerchantProperties(m, locale, country);

            if (merchantFound) {
                updatedMerchants.add(m);
                continue;
            }

            boolean addressFound = tryCopyAddressProperties(m, locale, country);

            if (addressFound) {
                updatedMerchants.add(m);
            }

        }

        return updatedMerchants;
    }

    private boolean tryCopyMerchantProperties(Merchant m, String locale, String country) throws Exception {
        Merchant googleMerchant = lookupMerchant(m.getName(), m.getFormattedAddress(), locale, country);

        if (googleMerchant != null) {
            copyGoogleMerchantProperties(googleMerchant, m);
            return true;
        }

        return false;
    }

    private boolean tryCopyAddressProperties(Merchant m, String locale, String country) throws Exception {
        Merchant googleMerchant = lookupAddress(m.getFormattedAddress(), locale, country);

        if (googleMerchant != null) {
            copyGoogleAddressProperties(googleMerchant, m);
            return true;
        }

        return false;
    }

    /**
     * Skip those merchants that already have locations or merchants without address or google
     *
     * @param merchant
     * @return
     */
    private boolean shouldIgnoreMerchant(Merchant merchant) {
        return merchant.getCoordinates() != null || Strings.isNullOrEmpty(merchant.getFormattedAddress()) || merchant
                .getSource().equals(MerchantSources.GOOGLE);
    }

    /**
     * Query google for an address and return a merchant with result
     */
    private Merchant lookupAddress(String address, String locale, String country) throws Exception {
        // Delay google api calls so we don't spam to much
        Thread.sleep(googleDelay);

        List<Place> searchResults;

        try {
            searchResults = searcher.detailedAutocomplete(address, 1, locale, country);
        }catch (Exception e){
            log.error(String.format("Could not call google api for address: [%s]", address), e);
            return null;
        }

        if (searchResults.size() == 0 || searchResults.get(0) == null) {
            log.info(String.format("Querying google with no results for address: [%s]", address));
            return null;
        }

        Place place = searchResults.get(0);
        log.info(String.format("Querying google for address: [%s] got result: [%s]", address, place.getAddress()));

        Merchant merchant = MerchantUtils.createMerchant(place, MerchantSources.GOOGLE);
        
        return assertAddressSimilarityThreshold(merchant, address);
    }

    /**
     * Query google for an a merchant bases on name and address and return result
     */
    private Merchant lookupMerchant(String name, String address, String locale, String country) throws Exception {

        // Delay google api calls so we don't spam to much
        Thread.sleep(googleDelay);

        List<Place> searchResults;

        try {
            searchResults = searcher.detailedAutocomplete(String.format("%s, %s", name, address), 1, locale, country);
        }catch (Exception e){
            log.error(String.format("Could not call google api for merchant: [%s], [%s]", name, address), e);
            return null;
        }

        if (searchResults.size() == 0 || searchResults.get(0) == null) {
            log.info(String.format("Querying google with no results for merchant: [%s], [%s]", name, address));
            return null;
        }

        Place place = searchResults.get(0);
        log.info(String.format("Querying google for merchant: [%s], [%s] got result [%s], [%s]", name, address,
                place.getName(), place.getAddress()));

        Merchant merchant = MerchantUtils.createMerchant(place, MerchantSources.GOOGLE);
        
        return assertNameSimilarityThreshold(assertAddressSimilarityThreshold(merchant, address), name);

    }

    private Merchant assertAddressSimilarityThreshold(Merchant merchant, String address) {
        if (merchant == null) {
            return null;
        }

        // Add Sweden as part of the similarity check since all merchants (right now) are in sweden
        double similarity = Math.max(StringUtils.getJaroWinklerDistance(address, merchant.getFormattedAddress()),
                StringUtils.getJaroWinklerDistance(address + ", Sweden", merchant.getFormattedAddress()));

        if (similarity < addressSimilarityThreshold) {
            log.info(String.format("Failing due to address similarity threshold. Current %.2f. Threshold: %s",
                    similarity, addressSimilarityThreshold));
            return null;

        }
        return merchant;
    }

    private Merchant assertNameSimilarityThreshold(Merchant merchant, String name) {
        if (merchant == null) {
            return null;
        }

        double similarity = StringUtils.getJaroWinklerDistance(name, merchant.getName());

        if (similarity < merchantNameSimilarityThreshold) {
            log.info(String.format("Failing due to name similarity threshold. Current %.2f. Thresholds: %s", similarity,
                    merchantNameSimilarityThreshold));
            return null;
        }

        return merchant;
    }

    private void copyGoogleMerchantProperties(Merchant googleMerchant, Merchant result) {
        if (googleMerchant == null) {
            return;
        }

        result.setWebsite(googleMerchant.getWebsite());
        result.setReference(googleMerchant.getReference());
        result.setPhotoAttributions(googleMerchant.getPhotoAttributions());
        result.setPhotoReference(googleMerchant.getPhotoReference());

        copyGoogleAddressProperties(googleMerchant, result);
    }

    private void copyGoogleAddressProperties(Merchant googleMerchant, Merchant result) {
        if (googleMerchant == null) {
            return;
        }

        result.setCountry(googleMerchant.getCountry());
        result.setCoordinates(googleMerchant.getCoordinates());

        if (Strings.isNullOrEmpty(result.getAddress())) {
            result.setAddress(googleMerchant.getAddress());
        }

        if (Strings.isNullOrEmpty(result.getCity())) {
            result.setCity(googleMerchant.getCity());
        }

        if (Strings.isNullOrEmpty(result.getPostalCode())) {
            result.setPostalCode(googleMerchant.getPostalCode());
        }

    }

}
