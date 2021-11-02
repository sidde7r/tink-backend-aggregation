package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner;

public class NordeaPartnerMarketUtil {

    public static String getLocaleDescription(String market) {
        String locale = getCountry(market);
        if (locale.equalsIgnoreCase("NO")) {
            return "Transaksjon";
        }
        if (locale.equalsIgnoreCase("FI")) {
            return "Transaktio";
        }
        if (locale.equalsIgnoreCase("SE") || locale.equalsIgnoreCase("DK")) {
            return "Transaktion";
        } else {
            return "Transaction";
        }
    }

    public static String getCountry(String market) {
        return market.substring(market.length() - 2);
    }

    public static boolean isNorway(String market) {
        String country = getCountry(market);
        return country.equalsIgnoreCase("NO");
    }
}
