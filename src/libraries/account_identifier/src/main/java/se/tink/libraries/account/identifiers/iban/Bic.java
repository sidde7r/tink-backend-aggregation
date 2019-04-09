package se.tink.libraries.account.identifiers.iban;

import com.google.common.collect.Maps;
import java.util.Map;

/** Stores bic numbers for different banks */
public class Bic {

    private static Map<String, String> map;

    static {
        map = Maps.newHashMap();

        map.put("at-erste-bank-and-sparkasse", "GIBAATWWXXX");
        map.put("gr-national-bank-of-greece", "ETHNGRAA");
    }

    public static String getBicByProviderName(String name) {
        return map.get(name);
    }
}
