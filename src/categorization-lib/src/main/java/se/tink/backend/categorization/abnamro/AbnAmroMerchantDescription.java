package se.tink.backend.categorization.abnamro;

import com.google.common.collect.ImmutableMap;
import se.tink.backend.categorization.CategorizationVector;
import se.tink.backend.categorization.api.AbnAmroCategories;

/**
 * Special mapping between merchant descriptions and categorization vector for ABN AMRO.
 */
public class AbnAmroMerchantDescription {

    public static final ImmutableMap<String, CategorizationVector> CATEGORIZATION_VECTORS = ImmutableMap.<String, CategorizationVector>builder()

            .put("FUEL DISPENSER", createVector(AbnAmroCategories.Codes.EXPENSES_TRANSPORT_CAR))
            .put("SERVICE STATIONS", createVector(AbnAmroCategories.Codes.EXPENSES_TRANSPORT_CAR))
            .put("AUTOMOBILE PARKING", createVector(AbnAmroCategories.Codes.EXPENSES_TRANSPORT_CAR))
            .put("BRIDGE AND ROAD FEES", createVector(AbnAmroCategories.Codes.EXPENSES_TRANSPORT_CAR))
            .put("AUTOMOTIVE PARTS", createVector(AbnAmroCategories.Codes.EXPENSES_TRANSPORT_CAR))
            .put("HERTZ CORPORATION", createVector(AbnAmroCategories.Codes.EXPENSES_TRANSPORT_CAR))
            .put("AVIS RENT A CAR", createVector(AbnAmroCategories.Codes.EXPENSES_TRANSPORT_CAR))
            .put("AUTOMOBILE PARKING LOTS", createVector(AbnAmroCategories.Codes.EXPENSES_TRANSPORT_CAR))
            .put("AUTOMATED GASOLINE DISPEN", createVector(AbnAmroCategories.Codes.EXPENSES_TRANSPORT_CAR))
            .put("DOLLAR RENT A CAR", createVector(AbnAmroCategories.Codes.EXPENSES_TRANSPORT_CAR))

            .put("AIR CARRIERS, AIRLINES", createVector(AbnAmroCategories.Codes.EXPENSES_TRANSPORT_FLIGHTS))
            .put("ROYAL DUTCH AIRLINES(KLM)", createVector(AbnAmroCategories.Codes.EXPENSES_TRANSPORT_FLIGHTS))
            .put("EASYJET", createVector(AbnAmroCategories.Codes.EXPENSES_TRANSPORT_FLIGHTS))
            .put("AIR ARABIA", createVector(AbnAmroCategories.Codes.EXPENSES_TRANSPORT_FLIGHTS))
            .put("AIR ARABIA AIRLINE", createVector(AbnAmroCategories.Codes.EXPENSES_TRANSPORT_FLIGHTS))
            .put("AIR ASTANA-AIRSTANA", createVector(AbnAmroCategories.Codes.EXPENSES_TRANSPORT_FLIGHTS))
            .put("AIR BERLIN - AIRBERLIN", createVector(AbnAmroCategories.Codes.EXPENSES_TRANSPORT_FLIGHTS))
            .put("AIR CANADA - AIR CAN", createVector(AbnAmroCategories.Codes.EXPENSES_TRANSPORT_FLIGHTS))
            .put("ALASKA AIRLINES INC.", createVector(AbnAmroCategories.Codes.EXPENSES_TRANSPORT_FLIGHTS))
            .put("KLM", createVector(AbnAmroCategories.Codes.EXPENSES_TRANSPORT_FLIGHTS))
            .put("WESTJET AIRLINES", createVector(AbnAmroCategories.Codes.EXPENSES_TRANSPORT_FLIGHTS))
            .put("U.S. AIRWAYS", createVector(AbnAmroCategories.Codes.EXPENSES_TRANSPORT_FLIGHTS))
            .put("BRITISH AIRWAYS-BRITISH A", createVector(AbnAmroCategories.Codes.EXPENSES_TRANSPORT_FLIGHTS))

            .put("MEN'S AND WOMEN'S CLOTHIN", createVector(AbnAmroCategories.Codes.EXPENSES_SHOPPING_CLOTHES))
            .put("MEN'S AND WOMEN'S CLOTHING", createVector(AbnAmroCategories.Codes.EXPENSES_SHOPPING_CLOTHES))
            .put("WOMEN'S READY TO WEAR", createVector(AbnAmroCategories.Codes.EXPENSES_SHOPPING_CLOTHES))
            .put("MEN'S AND BOY'S CLOTHING", createVector(AbnAmroCategories.Codes.EXPENSES_SHOPPING_CLOTHES))
            .put("CLOCK, JEWELRY, WATCH", createVector(AbnAmroCategories.Codes.EXPENSES_SHOPPING_CLOTHES))
            .put("CLOTHING FAMILY", createVector(AbnAmroCategories.Codes.EXPENSES_SHOPPING_CLOTHES))
            .put("CLOTHING - MEN'S AND WOME", createVector(AbnAmroCategories.Codes.EXPENSES_SHOPPING_CLOTHES))
            .put("CLOTHING - SPORTS, RIDING", createVector(AbnAmroCategories.Codes.EXPENSES_SHOPPING_CLOTHES))
            .put("CLOTHING - WOMEN'S", createVector(AbnAmroCategories.Codes.EXPENSES_SHOPPING_CLOTHES))
            .put("CLOTHING-MEN'S AND BOYS'S", createVector(AbnAmroCategories.Codes.EXPENSES_SHOPPING_CLOTHES))

            .put("MISCELLANEOUS FOOD STORES", createVector(AbnAmroCategories.Codes.EXPENSES_SHOPPING_BOOKS))
            .put("NEWS DEALERS AND NEWSSTAN", createVector(AbnAmroCategories.Codes.EXPENSES_SHOPPING_BOOKS))
            .put("BOOKS,NEWSPAPERS", createVector(AbnAmroCategories.Codes.EXPENSES_SHOPPING_BOOKS))

            .put("MUSIC STORES", createVector(AbnAmroCategories.Codes.EXPENSES_SHOPPING_OTHER))
            .put("RECORD SHOPS", createVector(AbnAmroCategories.Codes.EXPENSES_SHOPPING_OTHER))
            .put("HOME SUPPLY WAREHOUSE", createVector(AbnAmroCategories.Codes.EXPENSES_SHOPPING_OTHER))
            .put("MISCELLANEOUS RETAIL STOR", createVector(AbnAmroCategories.Codes.EXPENSES_SHOPPING_OTHER))
            .put("ANTIQUE SHOPS", createVector(AbnAmroCategories.Codes.EXPENSES_SHOPPING_OTHER))

            .put("ELECTRONIC SALES", createVector(AbnAmroCategories.Codes.EXPENSES_SHOPPING_ELECTRONICS))
            .put("COMPUTERS", createVector(AbnAmroCategories.Codes.EXPENSES_SHOPPING_ELECTRONICS))
            .put("ELECOM EQUIPMENT", createVector(AbnAmroCategories.Codes.EXPENSES_SHOPPING_ELECTRONICS))

            .put("EATING PLACES", createVector(AbnAmroCategories.Codes.EXPENSES_FOOD_RESTAURANTS))
            .put("FAST-FOOD RESTAURANTS", createVector(AbnAmroCategories.Codes.EXPENSES_FOOD_RESTAURANTS))

            .put("GROCERY STORES", createVector(AbnAmroCategories.Codes.EXPENSES_FOOD_GROCERIES))

            .put("DUTY FREE STORES", createVector(AbnAmroCategories.Codes.EXPENSES_FOOD_ALCOHOL_TOBACCO))

            .put("SPORTING GOODS STORES", createVector(AbnAmroCategories.Codes.EXPENSES_ENTERTAINMENT_HOBBY))

            .put("ART DEALERS AND GALLERIES", createVector(AbnAmroCategories.Codes.EXPENSES_ENTERTAINMENT_CULTURE))
            .put("THEATRICAL PRODUCERS", createVector(AbnAmroCategories.Codes.EXPENSES_ENTERTAINMENT_CULTURE))

            .put("GAMBLING TRANSACTIONS", createVector(AbnAmroCategories.Codes.EXPENSES_ENTERTAINMENT_LOTTERIES))

            .put("SHERATON(SHERATON HOTELS)",
                    createVector(AbnAmroCategories.Codes.EXPENSES_ENTERTAINMENT_ACCOMMODATION))
            .put("MILLENIUM HOTELS",
                    createVector(AbnAmroCategories.Codes.EXPENSES_ENTERTAINMENT_ACCOMMODATION))
            .put("LODGING - HOTELS, MOTELS",
                    createVector(AbnAmroCategories.Codes.EXPENSES_ENTERTAINMENT_ACCOMMODATION))

            .put("LAWN AND GARDEN SUPPLIES", createVector(AbnAmroCategories.Codes.EXPENSES_HOUSE_GARDEN))

            .put("CHILDREN'S AND INFANT'S", createVector(AbnAmroCategories.Codes.EXPENSES_MISC_KIDS))

            .put("DENTAL AND MED LABORATORI", createVector(AbnAmroCategories.Codes.EXPENSES_WELLNESS_HEALTHCARE))
            .put("HOSPITAL EQUIPMENT", createVector(AbnAmroCategories.Codes.EXPENSES_WELLNESS_HEALTHCARE))
            .put("HEALTH PRACTITIONERS", createVector(AbnAmroCategories.Codes.EXPENSES_WELLNESS_HEALTHCARE))
            .put("DRUG STORES", createVector(AbnAmroCategories.Codes.EXPENSES_WELLNESS_HEALTHCARE))
            .put("DRUG STORES,PHARMACIES", createVector(AbnAmroCategories.Codes.EXPENSES_WELLNESS_HEALTHCARE))

            .put("COSMETIC STORES", createVector(AbnAmroCategories.Codes.EXPENSES_WELLNESS_BEAUTY))

            .put("BANKS - ATM CASH", createVector(AbnAmroCategories.Codes.EXPENSES_MISC_WITHDRAWALS))

            .put("COLLEGES,UNIVERSITIES", createVector(AbnAmroCategories.Codes.EXPENSES_MISC_EDUCATION))

            .put("TAX PAYMENTS", createVector(AbnAmroCategories.Codes.EXPENSES_HOME_TAXES))

            .put("LIMOUSINES AND TAXICABS", createVector(AbnAmroCategories.Codes.EXPENSES_TRANSPORT_TAXI))
            .put("LIMOUSINES/TAXICABS", createVector(AbnAmroCategories.Codes.EXPENSES_TRANSPORT_TAXI))

            .put("CABLE AND OTHER PAY TV", createVector(AbnAmroCategories.Codes.EXPENSES_HOME_COMMUNICATIONS))

            .put("EQUIPMENT,FURNITURE", createVector(AbnAmroCategories.Codes.EXPENSES_HOUSE_FITMENT))

            .build();

    private static CategorizationVector createVector(String categoryCode) {
        CategorizationVector categorizationVector = new CategorizationVector();
        categorizationVector.setDistribution(categoryCode, 1);
        return categorizationVector;
    }
}
